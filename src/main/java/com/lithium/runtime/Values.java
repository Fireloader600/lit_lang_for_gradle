package com.lithium.runtime;

import java.util.List;

public final class Values {
    private Values() {}

    public static String stringify(Object v) {
        if (v == null)         return "null";
        if (v instanceof Double d) {
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return String.valueOf(d.longValue());
            }
            return d.toString();
        }
        if (v instanceof List<?> list) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(", ");
                Object e = list.get(i);
                sb.append(e instanceof String ? "\"" + e + "\"" : stringify(e));
            }
            return sb.append("]").toString();
        }
        return String.valueOf(v);
    }

    public static boolean truthy(Object v) {
        if (v == null)              return false;
        if (v instanceof Boolean b) return b;
        if (v instanceof Double d)  return d != 0;
        if (v instanceof String s)  return !s.isEmpty();
        if (v instanceof List<?> l) return !l.isEmpty();
        return true;
    }

    public static double toNumber(Object v) {
        if (v instanceof Double d)  return d;
        if (v instanceof Boolean b) return b ? 1 : 0;
        if (v instanceof String s) {
            try { return Double.parseDouble(s); }
            catch (NumberFormatException e) {
                throw new LithiumError("cannot convert string to number: \"" + s + "\"");
            }
        }
        throw new LithiumError("cannot convert to number: " + v);
    }

    public static boolean equals(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        if (a instanceof Double da && b instanceof Double db) return da.doubleValue() == db.doubleValue();
        return a.equals(b);
    }

    public static String typeName(Object v) {
        if (v == null)            return "null";
        if (v instanceof Double)  return "number";
        if (v instanceof String)  return "string";
        if (v instanceof Boolean) return "bool";
        if (v instanceof List)    return "list";
        if (v instanceof NativeFunction || v instanceof UserFunction) return "function";
        return "object";
    }
}