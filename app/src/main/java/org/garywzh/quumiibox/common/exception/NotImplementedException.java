package org.garywzh.quumiibox.common.exception;

public class NotImplementedException extends FatalException {
    public NotImplementedException() {
        super("This method not implemented yet!");
    }
}
