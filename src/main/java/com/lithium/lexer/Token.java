package com.lithium.lexer;

public final class Token {
    public enum Type {
        IDENT, NUMBER, STRING,
        LBRACE, RBRACE, LPAREN, RPAREN, LBRACKET, RBRACKET,
        SEMI, COMMA, DOT, COLON,
        ASSIGN, PLUS, MINUS, STAR, SLASH, PERCENT,
        EQ, NEQ, LT, GT, LE, GE, AND, OR, NOT,
        KW_IMPORT, KW_LET, KW_IF, KW_ELSE, KW_WHILE, KW_END,
        KW_TRUE, KW_FALSE, KW_NULL, KW_FN, KW_RETURN,
        EOF
    }

    public final Type type;
    public final String lexeme;
    public final Object literal;
    public final int line;
    public final int column;

    public Token(Type type, String lexeme, Object literal, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return type + "('" + lexeme + "')@" + line + ":" + column;
    }
}