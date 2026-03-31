package com.easyid;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;

final class Signer {
    private Signer() {
    }

    static String sign(String secret, String timestamp, Map<String, String> query, byte[] body) {
        StringBuilder payload = new StringBuilder();
        if (query != null && !query.isEmpty()) {
            boolean first = true;
            for (Map.Entry<String, String> entry : new TreeMap<>(query).entrySet()) {
                if (!first) {
                    payload.append('&');
                }
                first = false;
                payload.append(entry.getKey()).append('=').append(entry.getValue());
            }
        }
        if (body.length > 0) {
            if (payload.length() > 0) {
                payload.append('&');
            }
            payload.append(new String(body, StandardCharsets.UTF_8));
        }
        String toSign = timestamp + "\n" + payload;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(toSign.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("easyid: sign request failed", e);
        }
    }
}
