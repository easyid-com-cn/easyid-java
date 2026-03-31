package com.easyid;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RiskService {
    private final Transport transport;

    RiskService(Transport transport) {
        this.transport = transport;
    }

    public ScoreResult score(ScoreRequest request) throws IOException, InterruptedException {
        return transport.requestJson("POST", "/v1/risk/score", null, request.toPayload(), ScoreResult.class);
    }

    public StoreFingerprintResult storeFingerprint(StoreFingerprintRequest request) throws IOException, InterruptedException {
        return transport.requestJson("POST", "/v1/device/fingerprint", null, request.toPayload(), StoreFingerprintResult.class);
    }

    public record ScoreRequest(String ip, @JsonProperty("device_fingerprint") String deviceFingerprint, @JsonProperty("device_id") String deviceId, String phone, String email, @JsonProperty("user_agent") String userAgent, String action, Integer amount, Map<String, Object> context) {
        Map<String, Object> toPayload() {
            Map<String, Object> payload = new HashMap<>();
            if (ip != null) payload.put("ip", ip);
            if (deviceFingerprint != null) payload.put("device_fingerprint", deviceFingerprint);
            if (deviceId != null) payload.put("device_id", deviceId);
            if (phone != null) payload.put("phone", phone);
            if (email != null) payload.put("email", email);
            if (userAgent != null) payload.put("user_agent", userAgent);
            if (action != null) payload.put("action", action);
            if (amount != null) payload.put("amount", amount);
            if (context != null) payload.put("context", context);
            return payload;
        }
    }

    public record ScoreResult(@JsonProperty("risk_score") int riskScore, List<String> reasons, String recommendation, Details details) {
    }

    public record Details(@JsonProperty("rule_score") Integer ruleScore, @JsonProperty("ml_score") Integer mlScore) {
    }

    public record StoreFingerprintRequest(@JsonProperty("device_id") String deviceId, Map<String, Object> fingerprint) {
        Map<String, Object> toPayload() {
            return Map.of("device_id", deviceId, "fingerprint", fingerprint);
        }
    }

    public record StoreFingerprintResult(@JsonProperty("device_id") String deviceId, boolean stored) {
    }
}
