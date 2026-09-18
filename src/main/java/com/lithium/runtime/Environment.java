package com.lithium.runtime;

import java.util.HashMap;
import java.util.Map;

public final class Environment {
    private final Environment parent;
    private final Map<String, Object> vars = new HashMap<>();

    public Environment() { this(null); }
    public Environment(Environment parent) { this.parent = parent; }

    public void define(String name, Object value) { vars.put(name, value); }

    public Object get(String name) {
        if (vars.containsKey(name)) return vars.get(name);
        if (parent != null)         return parent.get(name);
        throw new LithiumError("undefined variable '" + name + "'");
    }

    public boolean has(String name) {
        if (vars.containsKey(name)) return true;
        return parent != null && parent.has(name);
    }

    public void set(String name, Object value) {
        if (vars.containsKey(name)) { vars.put(name, value); return; }
        if (parent != null && parent.has(name)) { parent.set(name, value); return; }
        vars.put(name, value);
    }
}