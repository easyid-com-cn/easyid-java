package com.easyid;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EasyIDClientTest {
    private final List<HttpServer> servers = new ArrayList<>();
    private static final String KEY_ID = "ak_3f9a2b1c7d4e8f0a";
    private static final String SECRET = "sk_test";

    @AfterEach
    void tearDown() {
        servers.forEach(server -> server.stop(0));
        servers.clear();
    }

    @Test
    void fullCoverage() throws Exception {
        String baseUrl = startServer(exchange -> {
            String body = switch (exchange.getRequestURI().getPath()) {
                case "/v1/idcard/verify2" -> ok("{\"result\":true,\"match\":true,\"supplier\":\"aliyun\",\"score\":0.98}");
                case "/v1/idcard/verify3" -> ok("{\"result\":true,\"match\":true,\"supplier\":\"tencent\",\"score\":0.95}");
                case "/v1/ocr/idcard" -> ok("{\"side\":\"front\",\"name\":\"张三\",\"id_number\":\"110101199001011234\"}");
                case "/v1/phone/status" -> ok("{\"status\":\"real\",\"carrier\":\"移动\",\"province\":\"广东\",\"roaming\":false}");
                case "/v1/phone/verify3" -> ok("{\"result\":true,\"match\":true,\"supplier\":\"aliyun\",\"score\":0.99}");
                case "/v1/face/liveness" -> ok("{\"liveness\":true,\"score\":0.97,\"method\":\"passive\",\"frames_analyzed\":10,\"attack_type\":null}");
                case "/v1/face/compare" -> ok("{\"match\":true,\"score\":0.92}");
                case "/v1/face/verify" -> ok("{\"result\":true,\"supplier\":\"aliyun\",\"score\":0.96}");
                case "/v1/bank/verify4" -> ok("{\"result\":true,\"match\":true,\"bank_name\":\"工商银行\",\"supplier\":\"aliyun\",\"score\":0.99,\"masked_bank_card\":\"6222****1234\",\"card_type\":\"debit\"}");
                case "/v1/risk/score" -> ok("{\"risk_score\":30,\"reasons\":[\"new_device\"],\"recommendation\":\"allow\",\"details\":{\"rule_score\":null,\"ml_score\":null}}");
                case "/v1/device/fingerprint" -> ok("{\"device_id\":\"dev_abc\",\"stored\":true}");
                case "/v1/billing/balance" -> ok("{\"app_id\":\"app_001\",\"available_cents\":100000}");
                case "/v1/billing/records" -> ok("{\"total\":1,\"page\":1,\"records\":[{\"id\":1,\"app_id\":\"app_001\",\"request_id\":\"req_001\",\"change_cents\":-100,\"balance_before\":100100,\"balance_after\":100000,\"reason\":\"idcard_verify2\",\"operator\":\"system\",\"created_at\":1711900000}]}");
                default -> "{}";
            };
            assertNotNull(exchange.getRequestHeaders().getFirst("X-Key-ID"));
            assertNotNull(exchange.getRequestHeaders().getFirst("X-Timestamp"));
            assertNotNull(exchange.getRequestHeaders().getFirst("X-Signature"));
            assertTrue(exchange.getRequestHeaders().getFirst("User-Agent").startsWith("easyid-java/"));
            send(exchange, 200, "application/json", body);
        });

        EasyIDClient client = EasyIDClient.builder().keyId(KEY_ID).secret(SECRET).baseUrl(baseUrl).timeout(Duration.ofSeconds(5)).build();
        assertTrue(client.idCard().verify2(new IDCardService.Verify2Request("张三", "110101199001011234", null)).result());
        assertTrue(client.idCard().verify3(new IDCardService.Verify3Request("张三", "110101199001011234", "13800138000", null)).match());
        assertEquals("张三", client.idCard().ocr("front", "image".getBytes(StandardCharsets.UTF_8), "id.jpg").name());
        assertEquals("real", client.phone().status("13800138000").status());
        assertTrue(client.phone().verify3(new PhoneService.Verify3Request("张三", "110101199001011234", "13800138000")).result());
        assertTrue(client.face().liveness("passive", "video".getBytes(StandardCharsets.UTF_8), "video.mp4").liveness());
        assertTrue(client.face().compare("img1".getBytes(StandardCharsets.UTF_8), "img2".getBytes(StandardCharsets.UTF_8), "a.jpg", "b.jpg").match());
        assertTrue(client.face().verify(new FaceService.VerifyRequest("110101199001011234", "oss://bucket/key", null)).result());
        assertEquals("debit", client.bank().verify4(new BankService.Verify4Request("张三", "110101199001011234", "6222021234567890", "13800138000", null)).cardType());
        assertEquals(30, client.risk().score(new RiskService.ScoreRequest("1.2.3.4", null, "dev_abc", null, null, null, "login", null, null)).riskScore());
        assertTrue(client.risk().storeFingerprint(new RiskService.StoreFingerprintRequest("dev_abc", Map.of("canvas", "hash123"))).stored());
        assertEquals(100000, client.billing().balance("app_001").availableCents());
        assertEquals(1, client.billing().records("app_001", 1, 20).total());
    }

    @Test
    void errorHandlingAndValidation() throws Exception {
        String apiErrorUrl = startServer(exchange -> send(exchange, 200, "application/json", "{\"code\":1001,\"message\":\"invalid key_id\",\"request_id\":\"err-rid\",\"data\":null}"));
        EasyIDClient apiErrorClient = EasyIDClient.builder().keyId(KEY_ID).secret(SECRET).baseUrl(apiErrorUrl).build();
        APIError apiError = assertThrows(APIError.class, () -> apiErrorClient.phone().status("13800138000"));
        assertEquals(1001, apiError.code());

        String htmlErrorUrl = startServer(exchange -> send(exchange, 503, "text/html", "<html>503</html>"));
        EasyIDClient htmlErrorClient = EasyIDClient.builder().keyId(KEY_ID).secret(SECRET).baseUrl(htmlErrorUrl).build();
        assertThrows(IOException.class, () -> htmlErrorClient.phone().status("13800138000"));

        String jsonErrorUrl = startServer(exchange -> send(exchange, 500, "application/json", "{\"code\":5000,\"message\":\"internal server error\",\"request_id\":\"err-500\",\"data\":null}"));
        EasyIDClient jsonErrorClient = EasyIDClient.builder().keyId(KEY_ID).secret(SECRET).baseUrl(jsonErrorUrl).build();
        APIError jsonError = assertThrows(APIError.class, () -> jsonErrorClient.phone().status("13800138000"));
        assertEquals(5000, jsonError.code());

        assertThrows(IllegalArgumentException.class, () -> EasyIDClient.builder().keyId("sk_abc").secret(SECRET).build());
        assertThrows(IllegalArgumentException.class, () -> EasyIDClient.builder().keyId(KEY_ID).secret("").build());
    }

    private String startServer(ExchangeHandler handler) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> handler.handle(exchange));
        server.start();
        servers.add(server);
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    private static String ok(String data) {
        return "{\"code\":0,\"message\":\"success\",\"request_id\":\"test-rid\",\"data\":" + data + "}";
    }

    private static void send(com.sun.net.httpserver.HttpExchange exchange, int status, String contentType, String body) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", contentType);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    @FunctionalInterface
    interface ExchangeHandler {
        void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException;
    }
}
