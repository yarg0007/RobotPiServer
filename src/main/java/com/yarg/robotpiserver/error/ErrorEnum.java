package com.yarg.robotpiserver.error;

public enum ErrorEnum {
    EXISTING_CONNECTION("There is already a client connection in existence.", 1),
    UNKNOWN_ERROR("An unknown error occured. Unable to establish connection.", 2);

    private final String message;
    private final int id;

    private ErrorEnum(String message, int id) {
        this.message = message;
        this.id = id;
    }
}
