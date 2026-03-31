package com.easyid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class Transport {
    static final String DEFAULT_BASE_URL = "https://api.easyid.com";
    static final String VERSION = "1.0.0";
    private static final int MAX_RESPONSE_BYTES = 10 << 20;

    private final String keyId;
    private final String secret;
    private final String baseUrl;
    private final HttpClient httpClient;
    private final Duration timeout;
    private final ObjectMapper mapper;

    Transport(String keyId, String secret, String baseUrl, HttpClient httpClient, Duration timeout) {
        this.keyId = keyId;
        this.secret = secret;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.httpClient = httpClient;
        this.timeout = timeout;
        this.mapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    <T> T requestJson(String method, String path, Map<String, String> query, Object body, Class<T> dataType) throws IOException, InterruptedException {
        byte[] bodyBytes = body == null ? new byte[0] : mapper.writeValueAsBytes(body);
        return send(method, path, query, bodyBytes, "application/json", dataType);
    }

    <T> T requestMultipart(String path, Map<String, String> fields, List<MultipartFile> files, Class<T> dataType) throws IOException, InterruptedException {
        String boundary = "----easyid-" + UUID.randomUUID();
        byte[] bodyBytes = buildMultipartBody(boundary, fields, files);
        return send("POST", path, null, bodyBytes, "multipart/form-data; boundary=" + boundary, dataType);
    }

    private <T> T send(String method, String path, Map<String, String> query, byte[] body, String contentType, Class<T> dataType) throws IOException, InterruptedException {
        String timestamp = Long.toString(Instant.now().getEpochSecond());
        String signature = Signer.sign(secret, timestamp, query, body);
        String url = baseUrl + path + buildQueryString(query);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(url))
            .timeout(timeout)
            .header("Content-Type", contentType)
            .header("X-Key-ID", keyId)
            .header("X-Timestamp", timestamp)
            .header("X-Signature", signature)
            .header("User-Agent", "easyid-java/" + VERSION);

        requestBuilder.method(method, body.length == 0 ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofByteArray(body));
        HttpResponse<byte[]> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());
        if (response.body().length > MAX_RESPONSE_BYTES) {
            throw new IOException("easyid: response exceeds 10 MB limit");
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            APIError apiError = parseAPIError(response.body());
            if (apiError != null) {
                throw apiError;
            }
            throw new IOException("easyid: http status " + response.statusCode());
        }

        Envelope envelope = mapper.readValue(response.body(), Envelope.class);
        if (envelope.code != 0) {
            throw new APIError(envelope.code, envelope.message, envelope.request_id);
        }
        if (dataType == Void.class || envelope.data == null) {
            return null;
        }
        return mapper.treeToValue(envelope.data, dataType);
    }

    private APIError parseAPIError(byte[] body) {
        try {
            Envelope envelope = mapper.readValue(body, Envelope.class);
            if (envelope.code != 0) {
                return new APIError(envelope.code, envelope.message, envelope.request_id);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String buildQueryString(Map<String, String> query) {
        if (query == null || query.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder("?");
        boolean first = true;
        for (Map.Entry<String, String> entry : new java.util.TreeMap<>(query).entrySet()) {
            if (!first) {
                builder.append('&');
            }
            first = false;
            builder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                .append('=')
                .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return builder.toString();
    }

    private static byte[] buildMultipartBody(String boundary, Map<String, String> fields, List<MultipartFile> files) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] crlf = "\r\n".getBytes(StandardCharsets.UTF_8);
        for (Map.Entry<String, String> field : fields.entrySet()) {
            out.write(("--" + boundary).getBytes(StandardCharsets.UTF_8));
            out.write(crlf);
            out.write(("Content-Disposition: form-data; name=\"" + field.getKey() + "\"").getBytes(StandardCharsets.UTF_8));
            out.write(crlf);
            out.write(crlf);
            out.write(field.getValue().getBytes(StandardCharsets.UTF_8));
            out.write(crlf);
        }
        for (MultipartFile file : files) {
            out.write(("--" + boundary).getBytes(StandardCharsets.UTF_8));
            out.write(crlf);
            out.write(("Content-Disposition: form-data; name=\"" + file.fieldName() + "\"; filename=\"" + file.filename() + "\"").getBytes(StandardCharsets.UTF_8));
            out.write(crlf);
            out.write(("Content-Type: " + file.contentType()).getBytes(StandardCharsets.UTF_8));
            out.write(crlf);
            out.write(crlf);
            out.write(file.content());
            out.write(crlf);
        }
        out.write(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));
        out.write(crlf);
        return out.toByteArray();
    }

    record MultipartFile(String fieldName, String filename, String contentType, byte[] content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class Envelope {
        public int code;
        public String message;
        public String request_id;
        public com.fasterxml.jackson.databind.JsonNode data;
    }
}
