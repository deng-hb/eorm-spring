package com.denghb.eorm;

public class EormException extends RuntimeException {

    public EormException() {
        super("unintended");
    }

    public EormException(String message) {
        super(message);
    }
}
