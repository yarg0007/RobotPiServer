package com.yarg.gen.models;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class ConnectErrorResponse extends ConnectResponse {

    @SerializedName("errorCode")
    private Integer errorCode = null;

    public Integer getErrorCode() { return errorCode; }
    public void setErrorCode(Integer errorCode) { this.errorCode = errorCode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectErrorResponse that = (ConnectErrorResponse) o;
        return Objects.equals(errorCode, that.errorCode) && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorCode, super.hashCode());
    }
}
