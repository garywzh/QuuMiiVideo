package org.garywzh.quumiibox.common.exception;

import okhttp3.Response;

public class RemoteException extends Exception {
    public RemoteException(Response response) {
        this(response, null);
    }

    public RemoteException(Response response, Throwable tr) {
        super("remote failed with code: " + response.code(), tr);
    }
}
