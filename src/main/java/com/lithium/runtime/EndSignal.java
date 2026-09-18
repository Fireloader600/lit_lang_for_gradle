package com.lithium.runtime;

public final class EndSignal extends RuntimeException {
    public final int code;
    public EndSignal(int code) {
        super(null, null, false, false);
        this.code = code;
    }
}