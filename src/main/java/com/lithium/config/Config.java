package com.lithium.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Config {
    private final Map<String, Object> values = new LinkedHashMap<>();

    public void put(String key, Object value) { values.put(key, value); }
    public Object get(String key)             { return values.get(key); }

    public String getString(String key) {
        Object v = values.get(key);
        return v == null ? null : String.valueOf(v);
    }

    public Double getNumber(String key) {
        Object v = values.get(key);
        if (v == null) return null;
        if (v instanceof Double d) return d;
        return Double.parseDouble(String.valueOf(v));
    }

    public Boolean getBool(String key) {
        Object v = values.get(key);
        if (v == null) return null;
        if (v instanceof Boolean b) return b;
        return Boolean.parseBoolean(String.valueOf(v));
    }

    @SuppressWarnings("unchecked")
    public List<Object> getList(String key) {
        Object v = values.get(key);
        return v instanceof List ? (List<Object>) v : null;
    }

    public Map<String, Object> asMap() { return values; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Config{\n");
        for (Map.Entry<String, Object> e : values.entrySet()) {
            sb.append("  ").append(e.getKey()).append(" = ").append(e.getValue()).append('\n');
        }
        return sb.append('}').toString();
    }
}