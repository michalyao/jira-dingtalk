package me.yoryor.plugin.client;

public class DingDingException extends RuntimeException {
    public DingDingException(String message, Throwable cause) {
        super(message, cause);
    }

    public DingDingException(Throwable cause) {
        super(cause);
    }

    public DingDingException(String message) {
        super(message);
    }
}
