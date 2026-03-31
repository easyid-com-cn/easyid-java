package com.easyid;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FaceService {
    private final Transport transport;

    FaceService(Transport transport) {
        this.transport = transport;
    }

    public LivenessResult liveness(String mode, byte[] media, String filename) throws IOException, InterruptedException {
        Map<String, String> fields = new HashMap<>();
        if (mode != null && !mode.isEmpty()) {
            fields.put("mode", mode);
        }
        return transport.requestMultipart("/v1/face/liveness", fields, List.of(new Transport.MultipartFile("media", filename, "application/octet-stream", media)), LivenessResult.class);
    }

    public CompareResult compare(byte[] image1, byte[] image2, String filename1, String filename2) throws IOException, InterruptedException {
        return transport.requestMultipart(
            "/v1/face/compare",
            Map.of(),
            List.of(
                new Transport.MultipartFile("image1", filename1, "application/octet-stream", image1),
                new Transport.MultipartFile("image2", filename2, "application/octet-stream", image2)
            ),
            CompareResult.class
        );
    }

    public VerifyResult verify(VerifyRequest request) throws IOException, InterruptedException {
        return transport.requestJson("POST", "/v1/face/verify", null, request.toPayload(), VerifyResult.class);
    }

    public record LivenessResult(boolean liveness, double score, String method, @JsonProperty("frames_analyzed") int framesAnalyzed, @JsonProperty("attack_type") String attackType) {
    }

    public record CompareResult(boolean match, double score) {
    }

    public record VerifyRequest(@JsonProperty("id_number") String idNumber, @JsonProperty("media_key") String mediaKey, @JsonProperty("callback_url") String callbackUrl) {
        Map<String, Object> toPayload() {
            Map<String, Object> payload = new HashMap<>();
            payload.put("id_number", idNumber);
            if (mediaKey != null) {
                payload.put("media_key", mediaKey);
            }
            if (callbackUrl != null) {
                payload.put("callback_url", callbackUrl);
            }
            return payload;
        }
    }

    public record VerifyResult(boolean result, String supplier, double score) {
    }
}
