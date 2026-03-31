package com.easyid;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.regex.Pattern;

public final class EasyIDClient {
    private static final Pattern KEY_ID_PATTERN = Pattern.compile("^ak_[0-9a-f]+$");

    private final IDCardService idCard;
    private final PhoneService phone;
    private final FaceService face;
    private final BankService bank;
    private final RiskService risk;
    private final BillingService billing;

    private EasyIDClient(Builder builder) {
        if (!KEY_ID_PATTERN.matcher(builder.keyId).matches()) {
            throw new IllegalArgumentException("easyid: keyId must match ak_<hex>, got: " + builder.keyId);
        }
        if (builder.secret == null || builder.secret.isEmpty()) {
            throw new IllegalArgumentException("easyid: secret must not be empty");
        }
        HttpClient httpClient = builder.httpClient != null ? builder.httpClient : HttpClient.newBuilder().build();
        Duration timeout = builder.timeout != null ? builder.timeout : Duration.ofSeconds(30);
        Transport transport = new Transport(builder.keyId, builder.secret, builder.baseUrl, httpClient, timeout);
        this.idCard = new IDCardService(transport);
        this.phone = new PhoneService(transport);
        this.face = new FaceService(transport);
        this.bank = new BankService(transport);
        this.risk = new RiskService(transport);
        this.billing = new BillingService(transport);
    }

    public static Builder builder() {
        return new Builder();
    }

    public IDCardService idCard() {
        return idCard;
    }

    public PhoneService phone() {
        return phone;
    }

    public FaceService face() {
        return face;
    }

    public BankService bank() {
        return bank;
    }

    public RiskService risk() {
        return risk;
    }

    public BillingService billing() {
        return billing;
    }

    public static final class Builder {
        private String keyId;
        private String secret;
        private String baseUrl = Transport.DEFAULT_BASE_URL;
        private HttpClient httpClient;
        private Duration timeout;

        public Builder keyId(String keyId) {
            this.keyId = keyId;
            return this;
        }

        public Builder secret(String secret) {
            this.secret = secret;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public EasyIDClient build() {
            return new EasyIDClient(this);
        }
    }
}
