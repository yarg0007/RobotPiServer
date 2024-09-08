package com.yarg.robotpiserver.audio;

public class TargetDataLineException extends Exception {

    public TargetDataLineException() {
    }

    public TargetDataLineException(String message) {
        super(message);
    }

    public TargetDataLineException(String message, Throwable cause) {
        super(message, cause);
    }

    public TargetDataLineException(Throwable cause) {
        super(cause);
    }

    public TargetDataLineException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
