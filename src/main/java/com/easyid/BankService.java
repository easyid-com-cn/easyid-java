package com.easyid;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class BankService {
    private final Transport transport;

    BankService(Transport transport) {
        this.transport = transport;
    }

    public Verify4Result verify4(Verify4Request request) throws IOException, InterruptedException {
        return transport.requestJson("POST", "/v1/bank/verify4", null, request.toPayload(), Verify4Result.class);
    }

    public record Verify4Request(String name, @JsonProperty("id_number") String idNumber, @JsonProperty("bank_card") String bankCard, String mobile, @JsonProperty("trace_id") String traceId) {
        Map<String, Object> toPayload() {
            Map<String, Object> payload = new HashMap<>();
            payload.put("name", name);
            payload.put("id_number", idNumber);
            payload.put("bank_card", bankCard);
            if (mobile != null) {
                payload.put("mobile", mobile);
            }
            if (traceId != null) {
                payload.put("trace_id", traceId);
            }
            return payload;
        }
    }

    public record Verify4Result(boolean result, boolean match, @JsonProperty("bank_name") String bankName, String supplier, double score, @JsonProperty("masked_bank_card") String maskedBankCard, @JsonProperty("card_type") String cardType) {
    }
}
