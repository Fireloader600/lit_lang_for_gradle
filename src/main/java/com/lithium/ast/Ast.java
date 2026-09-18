package com.lithium.ast;

import java.util.List;

public final class Ast {

    public interface Node {}
    public interface Stmt extends Node {}
    public interface Expr extends Node {}

    private Ast() {}

    public static final class Program {
        public final List<Stmt> statements;
        public Program(List<Stmt> statements) { this.statements = statements; }
    }

    public static final class ImportStmt implements Stmt {
        public final String path;
        public final int line;
        public ImportStmt(String path, int line) { this.path = path; this.line = line; }
    }

    public static final class LetStmt implements Stmt {
        public final String name;
        public final Expr init;
        public final int line;
        public LetStmt(String name, Expr init, int line) {
            this.name = name; this.init = init; this.line = line;
        }
    }

    public static final class ExprStmt implements Stmt {
        public final Expr expr;
        public ExprStmt(Expr expr) { this.expr = expr; }
    }

    public static final class Block implements Stmt {
        public final List<Stmt> statements;
        public Block(List<Stmt> statements) { this.statements = statements; }
    }

    public static final class BlockCall implements Stmt {
        public final String name;
        public final List<Expr> args;
        public final Block body;
        public final int line;
        public BlockCall(String name, List<Expr> args, Block body, int line) {
            this.name = name; this.args = args; this.body = body; this.line = line;
        }
    }

    public static final class IfStmt implements Stmt {
        public final Expr cond;
        public final Block then;
        public final Stmt otherwise;
        public IfStmt(Expr cond, Block then, Stmt otherwise) {
            this.cond = cond; this.then = then; this.otherwise = otherwise;
        }
    }

    public static final class WhileStmt implements Stmt {
        public final Expr cond;
        public final Block body;
        public WhileStmt(Expr cond, Block body) { this.cond = cond; this.body = body; }
    }

    public static final class EndStmt implements Stmt {
        public final Expr code;
        public final int line;
        public EndStmt(Expr code, int line) { this.code = code; this.line = line; }
    }

    public static final class ReturnStmt implements Stmt {
        public final Expr value;
        public final int line;
        public ReturnStmt(Expr value, int line) { this.value = value; this.line = line; }
    }

    public static final class FnDecl implements Stmt {
        public final String name;
        public final List<String> params;
        public final Block body;
        public final int line;
        public FnDecl(String name, List<String> params, Block body, int line) {
            this.name = name; this.params = params; this.body = body; this.line = line;
        }
    }

    public static final class Literal implements Expr {
        public final Object value;
        public Literal(Object value) { this.value = value; }
    }

    public static final class Ident implements Expr {
        public final String name;
        public final int line;
        public Ident(String name, int line) { this.name = name; this.line = line; }
    }

    public static final class ListExpr implements Expr {
        public final List<Expr> elements;
        public ListExpr(List<Expr> elements) { this.elements = elements; }
    }

    public static final class Unary implements Expr {
        public final String op;
        public final Expr operand;
        public final int line;
        public Unary(String op, Expr operand, int line) {
            this.op = op; this.operand = operand; this.line = line;
        }
    }

    public static final class Binary implements Expr {
        public final String op;
        public final Expr left, right;
        public final int line;
        public Binary(String op, Expr left, Expr right, int line) {
            this.op = op; this.left = left; this.right = right; this.line = line;
        }
    }

    public static final class Call implements Expr {
        public final Expr callee;
        public final List<Expr> args;
        public final int line;
        public Call(Expr callee, List<Expr> args, int line) {
            this.callee = callee; this.args = args; this.line = line;
        }
    }

    public static final class Index implements Expr {
        public final Expr target, index;
        public final int line;
        public Index(Expr target, Expr index, int line) {
            this.target = target; this.index = index; this.line = line;
        }
    }
}