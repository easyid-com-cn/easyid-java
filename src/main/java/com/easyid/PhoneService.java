package com.easyid;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.Map;

public final class PhoneService {
    private final Transport transport;

    PhoneService(Transport transport) {
        this.transport = transport;
    }

    public StatusResult status(String phone) throws IOException, InterruptedException {
        return transport.requestJson("GET", "/v1/phone/status", Map.of("phone", phone), null, StatusResult.class);
    }

    public Verify3Result verify3(Verify3Request request) throws IOException, InterruptedException {
        return transport.requestJson("POST", "/v1/phone/verify3", null, request.toPayload(), Verify3Result.class);
    }

    public record StatusResult(String status, String carrier, String province, boolean roaming) {
    }

    public record Verify3Request(String name, @JsonProperty("id_number") String idNumber, String mobile) {
        Map<String, Object> toPayload() {
            return Map.of("name", name, "id_number", idNumber, "mobile", mobile);
        }
    }

    public record Verify3Result(boolean result, boolean match, String supplier, double score) {
    }
}
