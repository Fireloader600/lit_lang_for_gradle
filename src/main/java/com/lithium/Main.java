package com.lithium;

import com.lithium.ast.Ast;
import com.lithium.code.CodeRegistry;
import com.lithium.config.Config;
import com.lithium.config.ConfigParser;
import com.lithium.lexer.Lexer;
import com.lithium.lexer.Token;
import com.lithium.pack.PackMeta;
import com.lithium.parser.Parser;
import com.lithium.runtime.EndSignal;
import com.lithium.runtime.Interpreter;
import com.lithium.runtime.LithiumError;
import com.lithium.runtime.LitProject;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public final class Main {

    public static final String VERSION = "0.1.0";

    public static void main(String[] args) {
        if (args.length == 0) { usage(); System.exit(1); }
        String cmd = args[0];

        try {
            switch (cmd) {
                case "run"      -> cmdRun(arg(args, 1, "src/hello.lit"));
                case "config"   -> cmdConfig(arg(args, 1, "config/default.litc"));
                case "registry" -> cmdRegistry();
                case "pack"     -> cmdPack();
                case "repl"     -> cmdRepl();
                case "version", "-v", "--version" -> System.out.println("Lithium (lit) " + VERSION);
                case "help", "-h", "--help"       -> usage();
                default -> { System.err.println("unknown command: " + cmd); usage(); System.exit(1); }
            }
        } catch (LithiumError e) {
            System.err.println(e);
            System.exit(2);
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            System.exit(3);
        }
    }

    private static String arg(String[] a, int i, String dflt) {
        return a.length > i ? a[i] : dflt;
    }

    // ---------- run ----------
    private static void cmdRun(String file) throws IOException {
        String src = LitProject.readText(file);
        List<Token> toks = new Lexer(src, file).tokenize();
        Ast.Program prog = new Parser(toks, file).parse();
        Interpreter interp = new Interpreter();
        int code = interp.run(prog);
        System.exit(code);
    }

    // ---------- config ----------
    private static void cmdConfig(String file) throws IOException {
        String src = LitProject.readText(file);
        Config cfg = new ConfigParser(src, file).parse();
        System.out.println(cfg);
    }

    // ---------- registry ----------
    private static void cmdRegistry() {
        System.out.println("Registered native functions:");
        CodeRegistry.all().keySet().forEach(n -> System.out.println("  " + n));
    }

    // ---------- pack ----------
    private static void cmdPack() {
        PackMeta meta = PackMeta.loadDefault();
        System.out.println("pack_logo : " + meta.packLogo);
        System.out.println("library   : " + meta.library);
    }

    // ---------- repl ----------
    private static void cmdRepl() {
        Interpreter interp = new Interpreter();
        Scanner sc = new Scanner(System.in);
        System.out.println("Lithium (lit) " + VERSION + " REPL — 输入 :quit 退出");
        while (true) {
            System.out.print("lit> ");
            if (!sc.hasNextLine()) break;
            String line = sc.nextLine();
            if (line.equals(":quit") || line.equals(":q")) break;
            if (line.isBlank()) continue;
            try {
                List<Token> toks = new Lexer(line, "<repl>").tokenize();
                Ast.Program prog = new Parser(toks, "<repl>").parse();
                interp.run(prog);
            } catch (LithiumError e) {
                System.err.println(e);
            } catch (EndSignal e) {
                break;
            } catch (Exception e) {
                System.err.println("error: " + e.getMessage());
            }
        }
    }

    private static void usage() {
        System.out.println("""
            Lithium (lit) — 轻量级 JVM 脚本语言

            用法:
              lit run    <file.lit>      运行脚本（默认 src/hello.lit）
              lit config <file.litc>     解析配置文件（默认 config/default.litc）
              lit registry               列出所有已注册的原生函数
              lit pack                   打印 lit.json 中的包信息
              lit repl                   交互式解释器
              lit version                打印版本号
              lit help                   显示本帮助

            路径相对 lit 项目根（src/main/resources/），也支持直接传绝对路径。
            """);
    }
}