package com.denghb.eorm;

public class EOrmException extends RuntimeException {

    public EOrmException(String message) {
        super(message);
    }

    public EOrmException(String message, Throwable cause) {
        super(message, cause);
    }
}
