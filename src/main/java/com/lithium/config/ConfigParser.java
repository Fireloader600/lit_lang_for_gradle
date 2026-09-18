package com.lithium.config;

import com.lithium.runtime.LithiumError;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析配置文件：
 *
 * config(){
 *     key("value");
 *     # 注释
 *     list("list-name"){
 *         "Hello",
 *         "World!"
 *     }
 * }
 */
public final class ConfigParser {

    private final String src;
    private final String file;
    private int pos = 0;
    private int line = 1;

    public ConfigParser(String src, String file) {
        this.src = src;
        this.file = file;
    }

    public Config parse() {
        Config cfg = new Config();
        skipTrivia();

        if (matchWord("config")) {
            skipTrivia();
            expect('(');
            skipTrivia();
            expect(')');
            skipTrivia();
            expect('{');
            parseBody(cfg);
            skipTrivia();
            expect('}');
        } else if (peek() == '{') {
            advance();
            parseBody(cfg);
            skipTrivia();
            expect('}');
        } else {
            parseBody(cfg);
        }
        return cfg;
    }

    private void parseBody(Config cfg) {
        while (true) {
            skipTrivia();
            if (atEnd() || peek() == '}') return;

            String key = readIdent();
            skipTrivia();

            if (peek() == '(') {
                advance();
                skipTrivia();
                List<Object> args = new ArrayList<>();
                if (peek() != ')') {
                    while (true) {
                        skipTrivia();
                        args.add(readValue());
                        skipTrivia();
                        if (peek() == ',') { advance(); continue; }
                        break;
                    }
                }
                expect(')');
                skipTrivia();
                if (peek() == '{') {
                    advance();
                    List<Object> items = new ArrayList<>();
                    while (true) {
                        skipTrivia();
                        if (peek() == '}') { advance(); break; }
                        items.add(readValue());
                        skipTrivia();
                        if (peek() == ',') advance();
                    }
                    cfg.put(key, items);
                } else if (peek() == ';') {
                    advance();
                    if (args.size() == 1) cfg.put(key, args.get(0));
                    else                  cfg.put(key, args);
                } else {
                    if (args.size() == 1) cfg.put(key, args.get(0));
                    else                  cfg.put(key, args);
                }
            } else if (peek() == '=') {
                advance();
                skipTrivia();
                Object v = readValue();
                cfg.put(key, v);
                skipTrivia();
                if (peek() == ';') advance();
            } else {
                throw err("expect '(' or '=' after key '" + key + "'");
            }
        }
    }

    private Object readValue() {
        skipTrivia();
        char c = peek();
        if (c == '"') return readString();
        if (c >= '0' && c <= '9' || c == '-' || c == '+') return readNumber();
        if (matchWord("true"))  return Boolean.TRUE;
        if (matchWord("false")) return Boolean.FALSE;
        if (matchWord("null"))  return null;
        return readIdent();
    }

    private String readString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (!atEnd() && peek() != '"') {
            char c = advance();
            if (c == '\\' && !atEnd()) {
                char n = advance();
                sb.append(switch (n) {
                    case 'n'  -> '\n';
                    case 't'  -> '\t';
                    case 'r'  -> '\r';
                    case '"'  -> '"';
                    case '\\' -> '\\';
                    default   -> n;
                });
            } else sb.append(c);
        }
        expect('"');
        return sb.toString();
    }

    private Double readNumber() {
        StringBuilder sb = new StringBuilder();
        if (peek() == '-' || peek() == '+') sb.append(advance());
        while (!atEnd() && (Character.isDigit(peek()) || peek() == '.')) sb.append(advance());
        return Double.parseDouble(sb.toString());
    }

    private String readIdent() {
        skipTrivia();
        if (!Character.isLetter(peek()) && peek() != '_') throw err("expect identifier");
        StringBuilder sb = new StringBuilder();
        while (!atEnd() && (Character.isLetterOrDigit(peek())
                || peek() == '_' || peek() == '-' || peek() == '.')) {
            sb.append(advance());
        }
        return sb.toString();
    }

    private void skipTrivia() {
        while (!atEnd()) {
            char c = peek();
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') { advance(); }
            else if (c == '#') { while (!atEnd() && peek() != '\n') advance(); }
            else if (c == '/' && peek(1) == '/') { while (!atEnd() && peek() != '\n') advance(); }
            else return;
        }
    }

    private boolean matchWord(String word) {
        skipTrivia();
        int end = pos + word.length();
        if (end > src.length()) return false;
        if (!src.startsWith(word, pos)) return false;
        if (end < src.length()) {
            char n = src.charAt(end);
            if (Character.isLetterOrDigit(n) || n == '_') return false;
        }
        pos = end;
        return true;
    }

    private boolean atEnd() { return pos >= src.length(); }
    private char peek()     { return peek(0); }
    private char peek(int o){ int i = pos + o; return i >= src.length() ? '\0' : src.charAt(i); }
    private char advance()  {
        char c = src.charAt(pos++);
        if (c == '\n') line++;
        return c;
    }
    private void expect(char c) {
        skipTrivia();
        if (atEnd() || peek() != c) throw err("expect '" + c + "'");
        advance();
    }
    private LithiumError err(String msg) {
        return new LithiumError(file + ": " + msg + " (line " + line + ")", line, -1);
    }
}