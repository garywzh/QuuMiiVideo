package org.garywzh.quumiibox.common.exception;

import com.squareup.okhttp.Response;

public class UnauthorizedException extends RequestException {
    public UnauthorizedException(Response response) {
        super(response);
    }

    public UnauthorizedException(Response response, Throwable tr) {
        super(response, tr);
    }
}
