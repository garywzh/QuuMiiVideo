package org.garywzh.quumiibox.common.exception;

public class FatalException extends RuntimeException {
    public FatalException(String detailMessage) {
        super(detailMessage);
    }

    public FatalException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public FatalException(Throwable throwable) {
        super(throwable);
    }
}
