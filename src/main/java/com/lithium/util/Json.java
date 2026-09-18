package com.lithium.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Json {
    private Json() {}

    public static Object parse(String s) {
        Parser p = new Parser(s);
        p.skipWs();
        Object v = p.value();
        p.skipWs();
        if (p.pos < s.length()) throw new IllegalArgumentException("trailing content in JSON");
        return v;
    }

    private static final class Parser {
        final String s;
        int pos = 0;
        Parser(String s) { this.s = s; }

        void skipWs() {
            while (pos < s.length()) {
                char c = s.charAt(pos);
                if (c == ' ' || c == '\t' || c == '\r' || c == '\n') pos++;
                else break;
            }
        }

        Object value() {
            skipWs();
            if (pos >= s.length()) throw new IllegalArgumentException("unexpected end of JSON");
            char c = s.charAt(pos);
            return switch (c) {
                case '{' -> object();
                case '[' -> array();
                case '"' -> string();
                case 't' -> { expect("true");  yield Boolean.TRUE; }
                case 'f' -> { expect("false"); yield Boolean.FALSE; }
                case 'n' -> { expect("null");  yield null; }
                default  -> number();
            };
        }

        Map<String, Object> object() {
            Map<String, Object> m = new LinkedHashMap<>();
            pos++;
            skipWs();
            if (peek() == '}') { pos++; return m; }
            while (true) {
                skipWs();
                String k = string();
                skipWs();
                if (peek() != ':') throw new IllegalArgumentException("expect ':'");
                pos++;
                Object v = value();
                m.put(k, v);
                skipWs();
                char c = peek();
                if (c == ',') { pos++; continue; }
                if (c == '}') { pos++; break; }
                throw new IllegalArgumentException("expect ',' or '}'");
            }
            return m;
        }

        List<Object> array() {
            List<Object> list = new ArrayList<>();
            pos++;
            skipWs();
            if (peek() == ']') { pos++; return list; }
            while (true) {
                list.add(value());
                skipWs();
                char c = peek();
                if (c == ',') { pos++; continue; }
                if (c == ']') { pos++; break; }
                throw new IllegalArgumentException("expect ',' or ']'");
            }
            return list;
        }

        String string() {
            if (peek() != '"') throw new IllegalArgumentException("expect '\"'");
            pos++;
            StringBuilder sb = new StringBuilder();
            while (pos < s.length()) {
                char c = s.charAt(pos++);
                if (c == '"') return sb.toString();
                if (c == '\\' && pos < s.length()) {
                    char n = s.charAt(pos++);
                    sb.append(switch (n) {
                        case 'n'  -> '\n';
                        case 't'  -> '\t';
                        case 'r'  -> '\r';
                        case 'b'  -> '\b';
                        case 'f'  -> '\f';
                        case '"'  -> '"';
                        case '\\' -> '\\';
                        case '/'  -> '/';
                        case 'u'  -> (char) Integer.parseInt(s.substring(pos, pos + 4), 16);
                        default   -> n;
                    });
                } else {
                    sb.append(c);
                }
            }
            throw new IllegalArgumentException("unterminated string");
        }

        Double number() {
            int start = pos;
            if (peek() == '-' || peek() == '+') pos++;
            while (pos < s.length() && (Character.isDigit(s.charAt(pos)) || s.charAt(pos) == '.'
                    || s.charAt(pos) == 'e' || s.charAt(pos) == 'E'
                    || s.charAt(pos) == '+' || s.charAt(pos) == '-')) pos++;
            return Double.parseDouble(s.substring(start, pos));
        }

        char peek() { return pos < s.length() ? s.charAt(pos) : '\0'; }

        void expect(String w) {
            if (!s.startsWith(w, pos)) throw new IllegalArgumentException("expect " + w);
            pos += w.length();
        }
    }
}