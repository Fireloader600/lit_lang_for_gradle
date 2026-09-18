package com.lithium.runtime;

import com.lithium.ast.Ast;
import com.lithium.code.CodeRegistry;
import com.lithium.lexer.Lexer;
import com.lithium.lexer.Token;
import com.lithium.parser.Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Interpreter {

    public final Environment globals = new Environment();
    private Environment env = globals;

    /** 已加载的库，避免重复执行。 */
    private final Set<String> loadedLibraries = new HashSet<>();

    public Interpreter() {
        CodeRegistry.installAll(this);
    }

    // ============================================================
    // 运行整个程序
    // ============================================================
    public int run(Ast.Program program) {
        try {
            for (Ast.Stmt s : program.statements) exec(s);
        } catch (EndSignal e) {
            return e.code;
        }
        return 0;
    }

    // ============================================================
    // 语句执行
    // ============================================================
    public void exec(Ast.Stmt s) {
        if (s instanceof Ast.ImportStmt im) {
            loadLibrary(im.path);
            return;
        }
        if (s instanceof Ast.LetStmt let) {
            Object v = let.init == null ? null : eval(let.init);
            env.define(let.name, v);
            return;
        }
        if (s instanceof Ast.ExprStmt es) {
            eval(es.expr);
            return;
        }
        if (s instanceof Ast.Block b) {
            execBlock(b, new Environment(env));
            return;
        }
        if (s instanceof Ast.BlockCall bc) {
            execBlockCall(bc);
            return;
        }
        if (s instanceof Ast.IfStmt is) {
            if (Values.truthy(eval(is.cond))) {
                execBlock(is.then, new Environment(env));
            } else if (is.otherwise != null) {
                if (is.otherwise instanceof Ast.Block b) execBlock(b, new Environment(env));
                else                                     exec(is.otherwise);
            }
            return;
        }
        if (s instanceof Ast.WhileStmt ws) {
            while (Values.truthy(eval(ws.cond))) {
                execBlock(ws.body, new Environment(env));
            }
            return;
        }
        if (s instanceof Ast.EndStmt es) {
            int code = es.code == null ? 0 : (int) Values.toNumber(eval(es.code));
            throw new EndSignal(code);
        }
        if (s instanceof Ast.ReturnStmt rs) {
            throw new ReturnSignal(rs.value == null ? null : eval(rs.value));
        }
        if (s instanceof Ast.FnDecl fn) {
            UserFunction uf = new UserFunction(fn.name, fn.params, fn.body, env);
            env.define(fn.name, uf);
            return;
        }
        throw new LithiumError("unknown statement: " + s.getClass().getSimpleName());
    }

    private void execBlock(Ast.Block block, Environment scope) {
        Environment saved = env;
        env = scope;
        try {
            for (Ast.Stmt s : block.statements) exec(s);
        } finally {
            env = saved;
        }
    }

    /** 处理 lit(){...} / config(){...} / 自定义块调用 */
    private void execBlockCall(Ast.BlockCall bc) {
        if ("lit".equals(bc.name) || "config".equals(bc.name)) {
            execBlock(bc.body, new Environment(env));
            return;
        }
        Object fn = env.get(bc.name);
        if (fn instanceof NativeFunction nf) {
            List<Object> args = new ArrayList<>();
            for (Ast.Expr a : bc.args) args.add(eval(a));
            nf.call(args, this);
            execBlock(bc.body, new Environment(env));
            return;
        }
        if (fn instanceof UserFunction uf) {
            List<Object> args = new ArrayList<>();
            for (Ast.Expr a : bc.args) args.add(eval(a));
            callUser(uf, args, bc.line);
            execBlock(bc.body, new Environment(env));
            return;
        }
        execBlock(bc.body, new Environment(env));
    }

    // ============================================================
    // import 加载库
    //   规则：
    //     import lit;        → 内置，忽略
    //     import foo.bar;    → library/foo/bar/main.lit
    //                        或 library/foo/bar.lit
    // ============================================================
    private void loadLibrary(String path) {
        if (path == null
                || path.isBlank()
                || "lit".equals(path)
                || "null".equalsIgnoreCase(path)) {
            return;
        }
        if (loadedLibraries.contains(path)) return;
        loadedLibraries.add(path);

        String base = path.replace('.', '/');

        // 1) 目录形式：library/foo/bar/main.lit
        String candidate = "library/" + base + "/main.lit";
        if (!LitProject.exists(candidate)) {
            // 2) 单文件形式：library/foo/bar.lit
            String alt = "library/" + base + ".lit";
            if (LitProject.exists(alt)) {
                candidate = alt;
            } else {
                throw new LithiumError("library not found: " + path
                        + " (tried /library/" + base + "/main.lit and /library/" + base + ".lit)");
            }
        }

        try {
            String src = LitProject.readText(candidate);
            List<Token> toks = new Lexer(src, candidate).tokenize();
            Ast.Program prog = new Parser(toks, candidate).parse();
            // 在当前环境下执行库代码，函数与全局变量会注册进当前作用域
            for (Ast.Stmt st : prog.statements) {
                exec(st);
            }
        } catch (IOException e) {
            throw new LithiumError("failed to load library " + path + ": " + e.getMessage());
        }
    }

    // ============================================================
    // 表达式求值
    // ============================================================
    public Object eval(Ast.Expr e) {
        if (e instanceof Ast.Literal lit) return lit.value;
        if (e instanceof Ast.Ident id)    return env.get(id.name);
        if (e instanceof Ast.ListExpr le) {
            List<Object> list = new ArrayList<>();
            for (Ast.Expr x : le.elements) list.add(eval(x));
            return list;
        }
        if (e instanceof Ast.Unary u) {
            Object v = eval(u.operand);
            return switch (u.op) {
                case "!"  -> !Values.truthy(v);
                case "-"  -> -Values.toNumber(v);
                default   -> throw new LithiumError("unknown unary op " + u.op, u.line, 0);
            };
        }
        if (e instanceof Ast.Binary b)  return evalBinary(b);
        if (e instanceof Ast.Call c)    return evalCall(c);
        if (e instanceof Ast.Index ix)  return evalIndex(ix);
        throw new LithiumError("unknown expression: " + e.getClass().getSimpleName());
    }

    private Object evalBinary(Ast.Binary b) {
        if ("=".equals(b.op)) {
            if (b.left instanceof Ast.Ident id) {
                Object v = eval(b.right);
                env.set(id.name, v);
                return v;
            }
            throw new LithiumError("invalid assignment target", b.line, 0);
        }
        if ("&&".equals(b.op)) {
            Object l = eval(b.left);
            if (!Values.truthy(l)) return false;
            return Values.truthy(eval(b.right));
        }
        if ("||".equals(b.op)) {
            Object l = eval(b.left);
            if (Values.truthy(l)) return true;
            return Values.truthy(eval(b.right));
        }

        Object l = eval(b.left);
        Object r = eval(b.right);

        switch (b.op) {
            case "+":
                if (l instanceof String || r instanceof String) {
                    return Values.stringify(l) + Values.stringify(r);
                }
                if (l instanceof List<?> la && r instanceof List<?> rb) {
                    List<Object> out = new ArrayList<>(la);
                    out.addAll(rb);
                    return out;
                }
                return Values.toNumber(l) + Values.toNumber(r);
            case "-": return Values.toNumber(l) - Values.toNumber(r);
            case "*": return Values.toNumber(l) * Values.toNumber(r);
            case "/": {
                double d = Values.toNumber(r);
                if (d == 0) throw new LithiumError("division by zero", b.line, 0);
                return Values.toNumber(l) / d;
            }
            case "%": {
                double d = Values.toNumber(r);
                if (d == 0) throw new LithiumError("modulo by zero", b.line, 0);
                return Values.toNumber(l) % d;
            }
            case "==": return Values.equals(l, r);
            case "!=": return !Values.equals(l, r);
            case "<":  return Values.toNumber(l) <  Values.toNumber(r);
            case ">":  return Values.toNumber(l) >  Values.toNumber(r);
            case "<=": return Values.toNumber(l) <= Values.toNumber(r);
            case ">=": return Values.toNumber(l) >= Values.toNumber(r);
        }
        throw new LithiumError("unknown binary op " + b.op, b.line, 0);
    }

    private Object evalCall(Ast.Call c) {
        Object callee = eval(c.callee);
        List<Object> args = new ArrayList<>();
        for (Ast.Expr a : c.args) args.add(eval(a));

        if (callee instanceof NativeFunction nf) return nf.call(args, this);
        if (callee instanceof UserFunction uf)   return callUser(uf, args, c.line);
        throw new LithiumError("not a function: " + Values.stringify(callee), c.line, 0);
    }

    private Object evalIndex(Ast.Index ix) {
        Object target = eval(ix.target);
        Object index  = eval(ix.index);
        if (target instanceof List<?> list) {
            int i = (int) Values.toNumber(index);
            if (i < 0 || i >= list.size())
                throw new LithiumError("index out of range: " + i, ix.line, 0);
            return list.get(i);
        }
        if (target instanceof String s) {
            int i = (int) Values.toNumber(index);
            if (i < 0 || i >= s.length())
                throw new LithiumError("index out of range: " + i, ix.line, 0);
            return String.valueOf(s.charAt(i));
        }
        throw new LithiumError("cannot index " + Values.typeName(target), ix.line, 0);
    }

    public Object callUser(UserFunction uf, List<Object> args, int line) {
        if (args.size() != uf.params.size()) {
            throw new LithiumError("function '" + uf.name + "' expects "
                    + uf.params.size() + " args, got " + args.size(), line, 0);
        }
        Environment scope = new Environment(uf.closure);
        for (int i = 0; i < uf.params.size(); i++) {
            scope.define(uf.params.get(i), args.get(i));
        }
        Environment saved = env;
        env = scope;
        try {
            for (Ast.Stmt s : uf.body.statements) exec(s);
            return null;
        } catch (ReturnSignal r) {
            return r.value;
        } finally {
            env = saved;
        }
    }

    // ============================================================
    // 辅助
    // ============================================================
    public void println(String s) { System.out.println(s); }
    public void print(String s)   { System.out.print(s); }
}