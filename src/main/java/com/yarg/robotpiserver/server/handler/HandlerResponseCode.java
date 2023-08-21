package com.yarg.robotpiserver.server.handler;

public enum HandlerResponseCode {
    SUCCESS(200),
    INTERNAL_SERVER_ERROR(500);

    private final int responseCode;

    private HandlerResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
