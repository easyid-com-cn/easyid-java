package com.easyid;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.Map;

public final class IDCardService {
    private final Transport transport;

    IDCardService(Transport transport) {
        this.transport = transport;
    }

    public VerifyResult verify2(Verify2Request request) throws IOException, InterruptedException {
        return transport.requestJson("POST", "/v1/idcard/verify2", null, request.toPayload(), VerifyResult.class);
    }

    public VerifyResult verify3(Verify3Request request) throws IOException, InterruptedException {
        return transport.requestJson("POST", "/v1/idcard/verify3", null, request.toPayload(), VerifyResult.class);
    }

    public OCRResult ocr(String side, byte[] image, String filename) throws IOException, InterruptedException {
        return transport.requestMultipart(
            "/v1/ocr/idcard",
            Map.of("side", side),
            java.util.List.of(new Transport.MultipartFile("image", filename, "application/octet-stream", image)),
            OCRResult.class
        );
    }

    public record Verify2Request(String name, @JsonProperty("id_number") String idNumber, @JsonProperty("trace_id") String traceId) {
        Map<String, Object> toPayload() {
            return traceId == null ? Map.of("name", name, "id_number", idNumber) : Map.of("name", name, "id_number", idNumber, "trace_id", traceId);
        }
    }

    public record Verify3Request(String name, @JsonProperty("id_number") String idNumber, String mobile, @JsonProperty("trace_id") String traceId) {
        Map<String, Object> toPayload() {
            return traceId == null
                ? Map.of("name", name, "id_number", idNumber, "mobile", mobile)
                : Map.of("name", name, "id_number", idNumber, "mobile", mobile, "trace_id", traceId);
        }
    }

    public record VerifyResult(boolean result, boolean match, String supplier, double score, Object raw) {
    }

    public record OCRResult(String side, String name, @JsonProperty("id_number") String idNumber, String gender, String nation, String birth, String address, String issue, String valid) {
    }
}
