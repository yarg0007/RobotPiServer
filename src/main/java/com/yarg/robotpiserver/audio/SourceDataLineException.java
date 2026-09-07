package com.yarg.robotpiserver.audio;

public class SourceDataLineException extends Exception {

    public SourceDataLineException() {
    }

    public SourceDataLineException(String message) {
        super(message);
    }

    public SourceDataLineException(String message, Throwable cause) {
        super(message, cause);
    }

    public SourceDataLineException(Throwable cause) {
        super(cause);
    }

    public SourceDataLineException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
