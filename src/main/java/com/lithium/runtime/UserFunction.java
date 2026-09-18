package com.lithium.runtime;

import com.lithium.ast.Ast;

import java.util.List;

public final class UserFunction {
    public final String name;
    public final List<String> params;
    public final Ast.Block body;
    public final Environment closure;

    public UserFunction(String name, List<String> params, Ast.Block body, Environment closure) {
        this.name = name;
        this.params = params;
        this.body = body;
        this.closure = closure;
    }
}