package com.lithium.lexer;

import com.lithium.runtime.LithiumError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Lexer {
    private static final Map<String, Token.Type> KEYWORDS = Map.ofEntries(
        Map.entry("import", Token.Type.KW_IMPORT),
        Map.entry("let",    Token.Type.KW_LET),
        Map.entry("if",     Token.Type.KW_IF),
        Map.entry("else",   Token.Type.KW_ELSE),
        Map.entry("while",  Token.Type.KW_WHILE),
        Map.entry("end",    Token.Type.KW_END),
        Map.entry("true",   Token.Type.KW_TRUE),
        Map.entry("false",  Token.Type.KW_FALSE),
        Map.entry("null",   Token.Type.KW_NULL),
        Map.entry("fn",     Token.Type.KW_FN),
        Map.entry("return", Token.Type.KW_RETURN)
    );

    private final String src;
    private final String file;
    private int pos = 0;
    private int line = 1;
    private int col  = 1;

    public Lexer(String src, String file) {
        this.src = src;
        this.file = file;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (true) {
            skipTrivia();
            if (atEnd()) break;
            tokens.add(scanToken());
        }
        tokens.add(new Token(Token.Type.EOF, "", null, line, col));
        return tokens;
    }

    private void skipTrivia() {
        while (!atEnd()) {
            char c = peek();
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                advance();
            } else if (c == '#') {
                while (!atEnd() && peek() != '\n') advance();
            } else if (c == '/' && peek(1) == '/') {
                while (!atEnd() && peek() != '\n') advance();
            } else {
                return;
            }
        }
    }

    private Token scanToken() {
        char c = peek();
        int sl = line, sc = col;

        if (isDigit(c))      return scanNumber(sl, sc);
        if (isIdentStart(c)) return scanIdentifier(sl, sc);
        if (c == '"')        return scanString(sl, sc);

        advance();
        switch (c) {
            case '{': return tok(Token.Type.LBRACE,   "{", sl, sc);
            case '}': return tok(Token.Type.RBRACE,   "}", sl, sc);
            case '(': return tok(Token.Type.LPAREN,   "(", sl, sc);
            case ')': return tok(Token.Type.RPAREN,   ")", sl, sc);
            case '[': return tok(Token.Type.LBRACKET, "[", sl, sc);
            case ']': return tok(Token.Type.RBRACKET, "]", sl, sc);
            case ';': return tok(Token.Type.SEMI,     ";", sl, sc);
            case ',': return tok(Token.Type.COMMA,    ",", sl, sc);
            case '.': return tok(Token.Type.DOT,      ".", sl, sc);
            case ':': return tok(Token.Type.COLON,    ":", sl, sc);
            case '+': return tok(Token.Type.PLUS,     "+", sl, sc);
            case '-': return tok(Token.Type.MINUS,    "-", sl, sc);
            case '*': return tok(Token.Type.STAR,     "*", sl, sc);
            case '/': return tok(Token.Type.SLASH,    "/", sl, sc);
            case '%': return tok(Token.Type.PERCENT,  "%", sl, sc);
            case '=':
                if (match('=')) return tok(Token.Type.EQ, "==", sl, sc);
                return tok(Token.Type.ASSIGN, "=", sl, sc);
            case '!':
                if (match('=')) return tok(Token.Type.NEQ, "!=", sl, sc);
                return tok(Token.Type.NOT, "!", sl, sc);
            case '<':
                if (match('=')) return tok(Token.Type.LE, "<=", sl, sc);
                return tok(Token.Type.LT, "<", sl, sc);
            case '>':
                if (match('=')) return tok(Token.Type.GE, ">=", sl, sc);
                return tok(Token.Type.GT, ">", sl, sc);
            case '&':
                if (match('&')) return tok(Token.Type.AND, "&&", sl, sc);
                break;
            case '|':
                if (match('|')) return tok(Token.Type.OR, "||", sl, sc);
                break;
        }
        throw new LithiumError(file + ": unexpected character '" + c + "'", sl, sc);
    }

    private Token scanNumber(int sl, int sc) {
        int start = pos;
        while (!atEnd() && isDigit(peek())) advance();
        if (!atEnd() && peek() == '.' && isDigit(peek(1))) {
            advance();
            while (!atEnd() && isDigit(peek())) advance();
        }
        String text = src.substring(start, pos);
        return new Token(Token.Type.NUMBER, text, Double.parseDouble(text), sl, sc);
    }

    private Token scanIdentifier(int sl, int sc) {
        int start = pos;
        while (!atEnd() && isIdentPart(peek())) advance();
        String text = src.substring(start, pos);
        Token.Type kw = KEYWORDS.get(text);
        return new Token(kw != null ? kw : Token.Type.IDENT, text, null, sl, sc);
    }

    private Token scanString(int sl, int sc) {
        advance();
        StringBuilder sb = new StringBuilder();
        while (!atEnd() && peek() != '"') {
            char c = advance();
            if (c == '\\' && !atEnd()) {
                char n = advance();
                switch (n) {
                    case 'n'  -> sb.append('\n');
                    case 't'  -> sb.append('\t');
                    case 'r'  -> sb.append('\r');
                    case '"'  -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    default   -> sb.append(n);
                }
            } else {
                sb.append(c);
            }
        }
        if (atEnd()) throw new LithiumError(file + ": unterminated string", sl, sc);
        advance();
        return new Token(Token.Type.STRING, sb.toString(), sb.toString(), sl, sc);
    }

    private Token tok(Token.Type t, String lex, int sl, int sc) {
        return new Token(t, lex, null, sl, sc);
    }

    private boolean atEnd() { return pos >= src.length(); }
    private char peek()     { return peek(0); }
    private char peek(int off) {
        int i = pos + off;
        return i >= src.length() ? '\0' : src.charAt(i);
    }
    private char advance() {
        char c = src.charAt(pos++);
        if (c == '\n') { line++; col = 1; } else { col++; }
        return c;
    }
    private boolean match(char expected) {
        if (atEnd() || src.charAt(pos) != expected) return false;
        advance();
        return true;
    }
    private static boolean isDigit(char c)      { return c >= '0' && c <= '9'; }
    private static boolean isIdentStart(char c) { return Character.isLetter(c) || c == '_'; }
    private static boolean isIdentPart(char c)  { return Character.isLetterOrDigit(c) || c == '_'; }
}