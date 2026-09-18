<div align="center">

# Lithium (lit)

**一门用 Java 实现的轻量级 JVM 脚本语言**

简单 · 可嵌入 · 可扩展

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://adoptium.net/)
[![Gradle](https://img.shields.io/badge/Gradle-8.0+-green.svg)](https://gradle.org/)
[![Version](https://img.shields.io/badge/version-0.1.0-orange.svg)]()

[快速开始](#-快速开始) · [语法](#-语法) · [CLI](#-命令行) · [库管理](#-库管理llp) · [打包](#-打包) · [编辑器支持](#-编辑器支持)

</div>

---

## 📖 简介

Lithium（简称 **lit**）是一门运行在 JVM 上的轻量级脚本语言。它的语法简洁、接近自然语言，适合用来自动化、写小工具、做教学演示，也可以嵌入到 Java 应用中当作脚本引擎使用。

整个项目是一个标准的 Gradle 工程，clone 下来即可编译、运行、打包。

```lit
import lit;

lit(){
    out("Hello World!");
    end 0;
}
```

---

## ✨ 特性

- **零依赖** — 纯 Java 实现，只依赖 JDK。
- **语法简洁** — 接近自然语言，几分钟就能上手。
- **可嵌入** — `Interpreter` 可以直接在你的 Java 项目里调用。
- **可扩展** — 用 Java 写原生函数，一行注册即可在 lit 中调用。
- **库管理** — 内置 `llp` 包管理器，一键安装、查看依赖。
- **打包友好** — `gradlew cl` 一条命令生成 `java -jar` 可执行包。
- **编辑器支持** — 附带 VSCode 扩展与 JetBrains 插件。

---

## 🚀 快速开始

### 环境要求

| 工具 | 版本 |
| --- | --- |
| JDK | 17 或更高 |
| Gradle | 8.0+（项目自带 wrapper，无需单独安装） |

### 克隆与构建

```bash
git clone https://github.com/your-username/lithium.git
cd lithium
./gradlew build
```

### 运行第一个脚本

```bash
./gradlew run
```

默认执行 `src/main/resources/src/hello.lit`：

```
Hello World!
```

### 运行其他脚本

```bash
./gradlew run -PlitArgs="run src/main.lit"
./gradlew run -PlitArgs="run src/fizzbuzz.lit"
./gradlew run -PlitArgs="run src/lists.lit"
```

---

## 📚 语法

### 1. 入口块

每个 lit 脚本都以 `import lit;` 开头，然后是 `lit(){ ... }` 块，程序从这里开始执行。

```lit
import lit;

lit(){
    out("Hello World!");
    end 0;
}
```

`end` 用于退出程序并指定退出码，`end 0` 表示正常退出。

---

### 2. 注释

```lit
# 行注释（推荐）

// 也可以
```

---

### 3. 变量

用 `let` 声明变量，类型动态。

```lit
lit(){
    let n     = 10;
    let name  = "Lithium";
    let flag  = true;
    let empty = null;
    let list  = [1, 2, 3];
    end 0;
}
```

| 类型 | 示例 |
| --- | --- |
| number | `42`、`3.14` |
| string | `"hello"`、`"你好"` |
| bool | `true`、`false` |
| null | `null` |
| list | `[1, 2, 3]`、`["a", "b"]` |
| function | `fn square(x){ return x * x; }` |

---

### 4. 运算符

```lit
let a = 10;
let b = 3;

out(a + b);       # 13
out(a - b);       # 7
out(a * b);       # 30
out(a / b);       # 3.3333333333333335
out(a % b);       # 1

out(a == b);      # false
out(a != b);      # true
out(a > b);       # true
out(a <= b);      # false

out(true && false);   # false
out(true || false);   # true
out(!true);           # false
```

字符串相加即拼接，列表相加即合并：

```lit
out("Hello, " + "World!");     # Hello, World!
out([1, 2] + [3, 4]);          # [1, 2, 3, 4]
```

---

### 5. 条件语句

```lit
lit(){
    let score = 85;

    if score >= 90 {
        out("优秀");
    } else if score >= 60 {
        out("及格");
    } else {
        out("不及格");
    }

    end 0;
}
```

---

### 6. 循环

```lit
lit(){
    let i = 1;
    while i <= 5 {
        out("i = " + str(i));
        i = i + 1;
    }
    end 0;
}
```

---

### 7. 函数

```lit
lit(){
    fn add(a, b) {
        return a + b;
    }

    fn greet(name) {
        out("Hello, " + name + "!");
    }

    out(add(2, 3));      # 5
    greet("Lithium");    # Hello, Lithium!

    end 0;
}
```

支持递归：

```lit
fn factorial(n) {
    if n <= 1 {
        return 1;
    }
    return n * factorial(n - 1);
}

out(factorial(5));   # 120
```

---

### 8. 列表

```lit
lit(){
    let nums = [1, 2, 3, 4, 5];

    out(len(nums));      # 5
    out(nums[0]);        # 1

    push(nums, 6);       # [1, 2, 3, 4, 5, 6]

    let i = 0;
    while i < len(nums) {
        out(nums[i]);
        i = i + 1;
    }

    end 0;
}
```

---

### 9. 内置函数

| 函数 | 说明 |
| --- | --- |
| `out(...)` | 输出一行，多个参数用空格连接 |
| `print(...)` | 输出，不换行 |
| `len(x)` | 字符串或列表的长度 |
| `str(x)` | 转字符串 |
| `num(x)` | 转数字 |
| `bool(x)` | 转布尔 |
| `type(x)` | 返回类型名（`"number"` / `"string"` / `"bool"` / `"list"` / `"null"` / `"function"`） |
| `upper(s)` | 转大写 |
| `lower(s)` | 转小写 |
| `split(s, sep)` | 按分隔符拆分字符串为列表 |
| `contains(x, y)` | 字符串 / 列表是否包含 |
| `push(list, x)` | 向列表末尾添加元素 |
| `range(n)` | 生成 `[0, 1, ..., n-1]` |
| `assert(cond, msg)` | 断言 |
| `exit(code)` | 退出（同 `end code`） |

示例：

```lit
out(len("hello"));                    # 5
out(upper("lithium"));                # LITHIUM
out(split("a,b,c", ","));             # ["a", "b", "c"]
out(range(5));                        # [0, 1, 2, 3, 4]
out(contains("hello", "ell"));        # true
out(type(42));                        # number
```

---

### 10. 导入库

用 `import <库名>;` 加载已安装的库。

```lit
import lit;
import com.example;

lit(){
    # 假设 com.example 里定义了 greet(name)
    greet("Lithium");
    end 0;
}
```

---

## 🗂 配置文件

除了 `.lit` 源码，lit 还支持一种简洁的配置文件格式（`.litc`）：

```litc
config(){
    key("value");

    # 注释
    list("list-name"){
        "Hello",
        "World!"
    }
}
```

也可以写多个参数（自动变为列表）：

```litc
config(){
    name("Lithium Demo");
    version("0.1.0");
    debug(true);

    # 多个参数 → 列表
    tags("language", "script", "jvm");

    # 键值对形式
    server = "localhost";
}
```

解析后可用 `Config` 读取：

```java
Config cfg = new ConfigParser(src, "app.litc").parse();

String        name    = cfg.getString("name");
Double        version = cfg.getNumber("version");
Boolean       debug   = cfg.getBool("debug");
List<Object>  tags    = cfg.getList("tags");
```

---

## 🧰 命令行

lit 的 CLI 由 `com.lithium.Main` 提供：

| 命令 | 说明 |
| --- | --- |
| `lit run <file.lit>` | 运行脚本，默认 `src/hello.lit` |
| `lit config <file.litc>` | 解析并打印配置文件 |
| `lit registry` | 列出所有已注册的原生函数 |
| `lit pack` | 打印 `lit.json` 中的包信息 |
| `lit repl` | 交互式解释器 |
| `lit version` | 打印版本号 |
| `lit help` | 显示帮助 |

通过 Gradle 调用：

```bash
./gradlew run -PlitArgs="run src/hello.lit"
./gradlew run -PlitArgs="config config/app.litc"
./gradlew run -PlitArgs="registry"
./gradlew run -PlitArgs="repl"
```

---

## 📦 库管理（llp）

Lithium 自带一个简单的包管理器 **llp**（Lithium Library Package）。

### 安装库

```bash
./gradlew llp -PllpArgs="install library/com/example.llp"
```

输出：

```
llp version:1.0.0
已安装库: com.example
库文件位于: src/main/resources/library/com.example/
```

安装完成后，`settings.gradle` 中的库列表会自动更新：

```groovy
// === LITHIUM LIBRARIES BEGIN ===
// 此列表由 `gradlew llp -PllpArgs="install <path>"` 自动维护，请勿手动修改
ext.lithiumLibraries = [
    "com.example",
]
// === LITHIUM LIBRARIES END ===
```

### 查看已安装的库

```bash
./gradlew llp -PllpArgs="list"
```

### 在 `lit.json` 中登记

```json
{
    "pack_logo": "logo.png",
    "library": [
        "library/com/example.llp"
    ]
}
```

没有依赖时，`library` 写 `null`：

```json
{
    "pack_logo": "logo.png",
    "library": null
}
```

### 在代码中使用

```lit
import lit;
import com.example;

lit(){
    greet("Lithium");
    end 0;
}
```

`import foo.bar;` 会按顺序尝试：

1. `library/foo/bar/main.lit`
2. `library/foo/bar.lit`

---

## 📦 打包

### 生成 `java -jar` 可执行包

```bash
./gradlew cl
```

产物：

```
build/java-jar/libs/Lithium-0.1.0.jar
```

运行：

```bash
java -jar build/java-jar/libs/Lithium-0.1.0.jar run src/hello.lit
java -jar build/java-jar/libs/Lithium-0.1.0.jar registry
java -jar build/java-jar/libs/Lithium-0.1.0.jar pack
```

### 生成 fat jar

```bash
./gradlew fatJar
```

产物：

```
build/libs/Lithium-0.1.0-all.jar
```

### 打包 lit 资源

```bash
./gradlew packLit
```

产物：

```
build/distributions/lithium-0.1.0-lit.zip
```

---

## 🧩 嵌入到 Java 项目

### 1. 依赖

```groovy
dependencies {
    implementation files('libs/Lithium-0.1.0-all.jar')
}
```

### 2. 执行脚本

```java
import com.lithium.ast.Ast;
import com.lithium.lexer.Lexer;
import com.lithium.lexer.Token;
import com.lithium.parser.Parser;
import com.lithium.runtime.Interpreter;

import java.util.List;

public class Demo {
    public static void main(String[] args) {
        String src = """
            import lit;
            lit(){
                out("Hello from Java!");
                end 0;
            }
            """;

        List<Token> tokens = new Lexer(src, "<inline>").tokenize();
        Ast.Program program = new Parser(tokens, "<inline>").parse();

        Interpreter interp = new Interpreter();
        int code = interp.run(program);

        System.out.println("exit code = " + code);
    }
}
```

### 3. 用 Java 注册自定义函数

```java
import com.lithium.code.CodeRegistry;

CodeRegistry.register("shout", (args, ctx) -> {
    ctx.println(String.valueOf(args.get(0)).toUpperCase());
    return null;
});
```

注册后在 lit 里就能调用：

```lit
import lit;

lit(){
    shout("hello");   # HELLO
    end 0;
}
```

---

## 🧱 项目结构

```
Lithium/
├── gradle.properties
├── settings.gradle
├── build.gradle
├── README.md
├── LICENSE
├── src/main/
│   ├── java/com/lithium/
│   │   ├── Main.java                 # CLI 入口
│   │   ├── lexer/                    # 词法分析
│   │   ├── ast/                      # AST 定义
│   │   ├── parser/                   # 语法分析
│   │   ├── runtime/                  # 解释器、环境、值
│   │   ├── code/CodeRegistry.java    # 原生函数注册
│   │   ├── config/                   # .litc 配置解析
│   │   ├── pack/PackMeta.java        # lit.json 读取
│   │   └── util/Json.java            # 内置极简 JSON 解析
│   └── resources/                    # lit 项目（会打进 jar）
│       ├── lit.json
│       ├── logo.png
│       ├── config/
│       │   ├── default.litc
│       │   └── app.litc
│       ├── library/                  # 由 llp 安装的第三方库
│       └── src/
│           ├── hello.lit
│           ├── main.lit
│           ├── fizzbuzz.lit
│           ├── lists.lit
│           └── strings.lit
├── vscode-extension/                 # VSCode 扩展
└── jetbrains-kotlin/                 # JetBrains 插件
```

---

## 🎨 编辑器支持

### VSCode

```bash
cd vscode-extension
npm install
npx vsce package
code --install-extension lithium-lang-0.1.0.vsix
```

获得：语法高亮、括号匹配、注释、代码片段、右键运行。

### JetBrains（IntelliJ IDEA / PyCharm / WebStorm 等）

```bash
cd jetbrains-kotlin
./gradlew buildPlugin
```

产物：`build/distributions/lithium-jetbrains-0.1.0.jar`

安装：**Settings → Plugins → ⚙ → Install Plugin from Disk…** 选择该 jar，重启 IDE。

获得：文件类型识别、语法高亮、颜色设置页、括号匹配、注释。

---

## 🗺 Roadmap

- [x] 词法 / 语法 / 解释器核心
- [x] 原生函数注册（`CodeRegistry`）
- [x] `.litc` 配置文件解析
- [x] `lit.json` 元数据读取
- [x] llp 库管理器
- [x] VSCode 扩展
- [x] JetBrains 插件
- [ ] 更完整的标准库（文件、时间、正则、网络）
- [ ] 模块系统与命名空间
- [ ] 调试器（DAP 桥接）
- [ ] LSP 服务器

---

## 🤝 贡献

欢迎提 Issue 和 Pull Request！

- 提交代码前请先 `./gradlew build` 确保通过。
- 提交新功能请附带对应的 `.lit` 示例脚本。
- 提交编辑器插件改动请分别在 `vscode-extension/` 与 `jetbrains-kotlin/` 下验证。

---

## 📄 License

本项目采用 **MIT License**，详见 [LICENSE](LICENSE)。

---

<div align="center">

**Lithium (lit)** · Made with ☕ and ❤️

</div>