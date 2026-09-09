package com.yarg.gen.models;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class DisconnectResponse {

    @SerializedName("message")
    private String message = null;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisconnectResponse that = (DisconnectResponse) o;
        return Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }
}
