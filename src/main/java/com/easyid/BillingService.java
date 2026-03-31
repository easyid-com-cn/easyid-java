package com.easyid;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class BillingService {
    private final Transport transport;

    BillingService(Transport transport) {
        this.transport = transport;
    }

    public BalanceResult balance(String appId) throws IOException, InterruptedException {
        return transport.requestJson("GET", "/v1/billing/balance", Map.of("app_id", appId), null, BalanceResult.class);
    }

    public RecordsResult records(String appId, int page, int pageSize) throws IOException, InterruptedException {
        int normalizedPageSize = pageSize <= 0 ? 20 : Math.min(pageSize, 100);
        return transport.requestJson(
            "GET",
            "/v1/billing/records",
            Map.of("app_id", appId, "page", Integer.toString(page > 0 ? page : 1), "page_size", Integer.toString(normalizedPageSize)),
            null,
            RecordsResult.class
        );
    }

    public record BalanceResult(@JsonProperty("app_id") String appId, @JsonProperty("available_cents") long availableCents) {
    }

    public record Record(long id, @JsonProperty("app_id") String appId, @JsonProperty("request_id") String requestId, @JsonProperty("change_cents") long changeCents, @JsonProperty("balance_before") long balanceBefore, @JsonProperty("balance_after") long balanceAfter, String reason, String operator, @JsonProperty("created_at") long createdAt) {
    }

    public record RecordsResult(long total, int page, List<Record> records) {
    }
}
