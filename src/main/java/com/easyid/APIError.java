package com.easyid;

public final class APIError extends RuntimeException {
    private final int code;
    private final String requestId;

    public APIError(int code, String message, String requestId) {
        super("easyid: code=" + code + " message=" + message + " request_id=" + requestId);
        this.code = code;
        this.requestId = requestId;
    }

    public int code() {
        return code;
    }

    public String requestId() {
        return requestId;
    }
}
