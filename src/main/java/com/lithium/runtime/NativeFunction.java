package com.lithium.runtime;

import java.util.List;

@FunctionalInterface
public interface NativeFunction {
    Object call(List<Object> args, Interpreter ctx);
}