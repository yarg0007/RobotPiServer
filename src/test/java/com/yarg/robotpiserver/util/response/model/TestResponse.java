package com.yarg.robotpiserver.util.response.model;

public class TestResponse {

    private int statusCode;
    private String payload;

    public int getStatusCode() {
        return statusCode;
    }

    public TestResponse setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public String getPayload() {
        return payload;
    }

    public TestResponse setPayload(String payload) {
        this.payload = payload;
        return this;
    }
}
