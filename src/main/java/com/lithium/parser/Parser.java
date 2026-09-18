package com.lithium.parser;

import com.lithium.ast.Ast;
import com.lithium.lexer.Token;
import com.lithium.runtime.LithiumError;

import java.util.ArrayList;
import java.util.List;

import static com.lithium.lexer.Token.Type.*;

public final class Parser {

    private final List<Token> tokens;
    private final String file;
    private int cur = 0;

    public Parser(List<Token> tokens, String file) {
        this.tokens = tokens;
        this.file = file;
    }

    public Ast.Program parse() {
        List<Ast.Stmt> stmts = new ArrayList<>();
        while (!check(EOF)) stmts.add(declaration());
        return new Ast.Program(stmts);
    }

    private Ast.Stmt declaration() {
        if (match(KW_IMPORT)) return importStmt();
        if (match(KW_LET))    return letStmt();
        if (match(KW_FN))     return fnDecl();
        if (match(KW_END))    return endStmt();
        if (match(KW_RETURN)) return returnStmt();
        if (match(KW_IF))     return ifStmt();
        if (match(KW_WHILE))  return whileStmt();
        return statement();
    }

    private Ast.Stmt importStmt() {
        Token t = consume(IDENT, "expect module name after 'import'");
        StringBuilder sb = new StringBuilder(t.lexeme);
        while (match(DOT)) {
            sb.append('.').append(consume(IDENT, "expect name after '.'").lexeme);
        }
        match(SEMI);
        return new Ast.ImportStmt(sb.toString(), t.line);
    }

    private Ast.Stmt letStmt() {
        Token name = consume(IDENT, "expect variable name after 'let'");
        Ast.Expr init = null;
        if (match(ASSIGN)) init = expression();
        match(SEMI);
        return new Ast.LetStmt(name.lexeme, init, name.line);
    }

    private Ast.Stmt fnDecl() {
        Token name = consume(IDENT, "expect function name after 'fn'");
        consume(LPAREN, "expect '(' after function name");
        List<String> params = new ArrayList<>();
        if (!check(RPAREN)) {
            do {
                params.add(consume(IDENT, "expect parameter name").lexeme);
            } while (match(COMMA));
        }
        consume(RPAREN, "expect ')' after parameters");
        Ast.Block body = block();
        return new Ast.FnDecl(name.lexeme, params, body, name.line);
    }

    private Ast.Stmt endStmt() {
        Token t = previous();
        Ast.Expr code = null;
        if (!check(SEMI) && !check(RBRACE) && !check(EOF)) code = expression();
        match(SEMI);
        return new Ast.EndStmt(code, t.line);
    }

    private Ast.Stmt returnStmt() {
        Token t = previous();
        Ast.Expr value = null;
        if (!check(SEMI) && !check(RBRACE) && !check(EOF)) value = expression();
        match(SEMI);
        return new Ast.ReturnStmt(value, t.line);
    }

    private Ast.Stmt ifStmt() {
        Ast.Expr cond = expression();
        Ast.Block then = block();
        Ast.Stmt otherwise = null;
        if (match(KW_ELSE)) {
            if (match(KW_IF)) otherwise = ifStmt();
            else              otherwise = block();
        }
        return new Ast.IfStmt(cond, then, otherwise);
    }

    private Ast.Stmt whileStmt() {
        Ast.Expr cond = expression();
        Ast.Block body = block();
        return new Ast.WhileStmt(cond, body);
    }

    private Ast.Stmt statement() {
        if (check(IDENT) && checkAt(1, LPAREN)) {
            return tryBlockCall();
        }
        if (check(LBRACE)) return block();
        return exprStmt();
    }

    private Ast.Stmt tryBlockCall() {
        int save = cur;
        Token name = advance();
        advance();
        List<Ast.Expr> args = new ArrayList<>();
        if (!check(RPAREN)) {
            do {
                if (check(RPAREN)) break;
                args.add(expression());
            } while (match(COMMA));
        }
        if (!match(RPAREN)) { cur = save; return exprStmt(); }
        if (!check(LBRACE)) { cur = save; return exprStmt(); }
        Ast.Block body = block();
        return new Ast.BlockCall(name.lexeme, args, body, name.line);
    }

    private Ast.Block block() {
        consume(LBRACE, "expect '{'");
        List<Ast.Stmt> stmts = new ArrayList<>();
        while (!check(RBRACE) && !check(EOF)) {
            stmts.add(declaration());
        }
        consume(RBRACE, "expect '}'");
        return new Ast.Block(stmts);
    }

    private Ast.Stmt exprStmt() {
        Ast.Expr e = expression();
        match(SEMI);
        return new Ast.ExprStmt(e);
    }

    private Ast.Expr expression() { return assignment(); }

    private Ast.Expr assignment() {
        Ast.Expr left = or();
        if (match(ASSIGN)) {
            Token eq = previous();
            Ast.Expr right = assignment();
            if (left instanceof Ast.Ident id) {
                return new Ast.Binary("=", new Ast.Ident(id.name, id.line), right, eq.line);
            }
            throw new LithiumError(file + ": invalid assignment target", eq.line, eq.column);
        }
        return left;
    }

    private Ast.Expr or() {
        Ast.Expr e = and();
        while (match(OR)) {
            Token op = previous();
            Ast.Expr r = and();
            e = new Ast.Binary("||", e, r, op.line);
        }
        return e;
    }

    private Ast.Expr and() {
        Ast.Expr e = equality();
        while (match(AND)) {
            Token op = previous();
            Ast.Expr r = equality();
            e = new Ast.Binary("&&", e, r, op.line);
        }
        return e;
    }

    private Ast.Expr equality() {
        Ast.Expr e = comparison();
        while (match(EQ, NEQ)) {
            Token op = previous();
            Ast.Expr r = comparison();
            e = new Ast.Binary(op.lexeme, e, r, op.line);
        }
        return e;
    }

    private Ast.Expr comparison() {
        Ast.Expr e = term();
        while (match(LT, GT, LE, GE)) {
            Token op = previous();
            Ast.Expr r = term();
            e = new Ast.Binary(op.lexeme, e, r, op.line);
        }
        return e;
    }

    private Ast.Expr term() {
        Ast.Expr e = factor();
        while (match(PLUS, MINUS)) {
            Token op = previous();
            Ast.Expr r = factor();
            e = new Ast.Binary(op.lexeme, e, r, op.line);
        }
        return e;
    }

    private Ast.Expr factor() {
        Ast.Expr e = unary();
        while (match(STAR, SLASH, PERCENT)) {
            Token op = previous();
            Ast.Expr r = unary();
            e = new Ast.Binary(op.lexeme, e, r, op.line);
        }
        return e;
    }

    private Ast.Expr unary() {
        if (match(NOT, MINUS)) {
            Token op = previous();
            Ast.Expr e = unary();
            return new Ast.Unary(op.lexeme, e, op.line);
        }
        return call();
    }

    private Ast.Expr call() {
        Ast.Expr e = primary();
        while (true) {
            if (match(LPAREN)) {
                Token p = previous();
                List<Ast.Expr> args = new ArrayList<>();
                if (!check(RPAREN)) {
                    do {
                        if (check(RPAREN)) break;
                        args.add(expression());
                    } while (match(COMMA));
                }
                consume(RPAREN, "expect ')' after arguments");
                e = new Ast.Call(e, args, p.line);
            } else if (match(LBRACKET)) {
                Token p = previous();
                Ast.Expr idx = expression();
                consume(RBRACKET, "expect ']' after index");
                e = new Ast.Index(e, idx, p.line);
            } else {
                break;
            }
        }
        return e;
    }

    private Ast.Expr primary() {
        if (match(KW_TRUE))  return new Ast.Literal(Boolean.TRUE);
        if (match(KW_FALSE)) return new Ast.Literal(Boolean.FALSE);
        if (match(KW_NULL))  return new Ast.Literal(null);
        if (match(NUMBER))   return new Ast.Literal(previous().literal);
        if (match(STRING))   return new Ast.Literal(previous().literal);

        if (match(IDENT)) {
            Token id = previous();
            StringBuilder sb = new StringBuilder(id.lexeme);
            while (match(DOT)) {
                sb.append('.').append(consume(IDENT, "expect name after '.'").lexeme);
            }
            return new Ast.Ident(sb.toString(), id.line);
        }

        if (match(LPAREN)) {
            Ast.Expr e = expression();
            consume(RPAREN, "expect ')' after expression");
            return e;
        }

        if (match(LBRACKET)) {
            List<Ast.Expr> elems = new ArrayList<>();
            if (!check(RBRACKET)) {
                do {
                    if (check(RBRACKET)) break;
                    elems.add(expression());
                } while (match(COMMA));
            }
            consume(RBRACKET, "expect ']' after list");
            return new Ast.ListExpr(elems);
        }

        Token t = peek();
        throw new LithiumError(file + ": unexpected token '" + t.lexeme + "'", t.line, t.column);
    }

    private boolean match(Token.Type... types) {
        for (Token.Type t : types) {
            if (check(t)) { advance(); return true; }
        }
        return false;
    }

    private boolean check(Token.Type t)   { return peek().type == t; }
    private boolean checkAt(int off, Token.Type t) {
        int i = cur + off;
        return i < tokens.size() && tokens.get(i).type == t;
    }

    private Token advance() {
        if (!check(EOF)) cur++;
        return previous();
    }

    private Token peek()     { return tokens.get(cur); }
    private Token previous() { return tokens.get(cur - 1); }

    private Token consume(Token.Type t, String msg) {
        if (check(t)) return advance();
        Token p = peek();
        throw new LithiumError(file + ": " + msg + " (got '" + p.lexeme + "')", p.line, p.column);
    }
}