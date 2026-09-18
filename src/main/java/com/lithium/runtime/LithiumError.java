package com.lithium.runtime;

public class LithiumError extends RuntimeException {
    public final int line;
    public final int column;

    public LithiumError(String message, int line, int column) {
        super(message);
        this.line = line;
        this.column = column;
    }

    public LithiumError(String message) {
        this(message, -1, -1);
    }

    @Override
    public String toString() {
        return line >= 0
                ? "LithiumError at " + line + ":" + column + " — " + getMessage()
                : "LithiumError — " + getMessage();
    }
}