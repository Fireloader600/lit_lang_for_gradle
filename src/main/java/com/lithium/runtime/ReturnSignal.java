package com.lithium.runtime;

public final class ReturnSignal extends RuntimeException {
    public final Object value;
    public ReturnSignal(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}