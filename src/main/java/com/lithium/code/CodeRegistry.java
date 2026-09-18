package com.lithium.code;

import com.lithium.runtime.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ★ 所有原生函数在这里注册，用 Java 实现。
 *   这是语言扩展的统一入口。
 */
public final class CodeRegistry {

    private static final Map<String, NativeFunction> FUNCTIONS = new LinkedHashMap<>();

    static {
        FUNCTIONS.put("out", (args, ctx) -> {
            ctx.println(joinArgs(args));
            return null;
        });

        FUNCTIONS.put("print", (args, ctx) -> {
            ctx.print(joinArgs(args));
            return null;
        });

        FUNCTIONS.put("len", (args, ctx) -> {
            checkArity("len", args, 1);
            Object v = args.get(0);
            if (v instanceof String s) return (double) s.length();
            if (v instanceof List<?> l) return (double) l.size();
            throw new LithiumError("len() expects string or list");
        });

        FUNCTIONS.put("str", (args, ctx) -> {
            checkArity("str", args, 1);
            return Values.stringify(args.get(0));
        });

        FUNCTIONS.put("num", (args, ctx) -> {
            checkArity("num", args, 1);
            return Values.toNumber(args.get(0));
        });

        FUNCTIONS.put("type", (args, ctx) -> {
            checkArity("type", args, 1);
            return Values.typeName(args.get(0));
        });

        FUNCTIONS.put("bool", (args, ctx) -> {
            checkArity("bool", args, 1);
            return Values.truthy(args.get(0));
        });

        FUNCTIONS.put("upper", (args, ctx) -> {
            checkArity("upper", args, 1);
            return Values.stringify(args.get(0)).toUpperCase();
        });

        FUNCTIONS.put("lower", (args, ctx) -> {
            checkArity("lower", args, 1);
            return Values.stringify(args.get(0)).toLowerCase();
        });

        FUNCTIONS.put("split", (args, ctx) -> {
            checkArity("split", args, 2);
            String s = Values.stringify(args.get(0));
            String sep = Values.stringify(args.get(1));
            List<Object> out = new ArrayList<>();
            for (String p : s.split(java.util.regex.Pattern.quote(sep))) out.add(p);
            return out;
        });

        FUNCTIONS.put("contains", (args, ctx) -> {
            checkArity("contains", args, 2);
            Object a = args.get(0), b = args.get(1);
            if (a instanceof String s)  return s.contains(Values.stringify(b));
            if (a instanceof List<?> l) return l.contains(b);
            return false;
        });

        FUNCTIONS.put("push", (args, ctx) -> {
            checkArity("push", args, 2);
            if (!(args.get(0) instanceof List<?> l))
                throw new LithiumError("push() expects list as first argument");
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) l;
            list.add(args.get(1));
            return list;
        });

        FUNCTIONS.put("range", (args, ctx) -> {
            checkArity("range", args, 1);
            int n = (int) Values.toNumber(args.get(0));
            List<Object> out = new ArrayList<>();
            for (int i = 0; i < n; i++) out.add((double) i);
            return out;
        });

        FUNCTIONS.put("exit", (args, ctx) -> {
            int code = args.isEmpty() ? 0 : (int) Values.toNumber(args.get(0));
            throw new EndSignal(code);
        });

        FUNCTIONS.put("assert", (args, ctx) -> {
            checkArity("assert", args, 2);
            if (!Values.truthy(args.get(0))) {
                throw new LithiumError("assertion failed: " + Values.stringify(args.get(1)));
            }
            return null;
        });
    }

    private CodeRegistry() {}

    public static void installAll(Interpreter interp) {
        for (Map.Entry<String, NativeFunction> e : FUNCTIONS.entrySet()) {
            interp.globals.define(e.getKey(), e.getValue());
        }
    }

    /** 供外部 Java 代码扩展注册。 */
    public static void register(String name, NativeFunction fn) {
        FUNCTIONS.put(name, fn);
    }

    public static Map<String, NativeFunction> all() {
        return Collections.unmodifiableMap(FUNCTIONS);
    }

    private static String joinArgs(List<Object> args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) sb.append(' ');
            sb.append(Values.stringify(args.get(i)));
        }
        return sb.toString();
    }

    private static void checkArity(String name, List<Object> args, int expected) {
        if (args.size() != expected) {
            throw new LithiumError(name + "() expects " + expected
                + " args, got " + args.size());
        }
    }
}