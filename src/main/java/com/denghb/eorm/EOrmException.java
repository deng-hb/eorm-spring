package com.denghb.eorm;

public class EOrmException extends RuntimeException {

    public EOrmException() {
        super("unintended");
    }

    public EOrmException(String message) {
        super(message);
    }
}
