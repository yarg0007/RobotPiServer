package com.yarg.gen.models;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class DisconnectRequest {

    @SerializedName("shutdown")
    private Boolean shutdown = false;

    public Boolean isShutdown() { return shutdown; }
    public void setShutdown(Boolean shutdown) { this.shutdown = shutdown; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisconnectRequest that = (DisconnectRequest) o;
        return Objects.equals(shutdown, that.shutdown);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shutdown);
    }
}
