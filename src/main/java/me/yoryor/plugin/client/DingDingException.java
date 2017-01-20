package me.yoryor.plugin.client;

/**
 * 所有异常都包装为未检查异常，避免各种麻烦的异常处理.
 */
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
