# Lithium (lit) — VSCode 扩展

为 [Lithium](https://github.com/your/lithium) 提供：

- `.lit` 与 `.litc` 语法高亮
- 括号匹配、自动闭合
- `#` 行注释
- 代码片段（`lit`、`out`、`if`、`while`、`fn`、`config` 等）
- 右键菜单：运行当前文件 / 解析当前配置
- 命令面板：`Lithium: 运行当前文件` 等

## 使用

1. 构建 Lithium 项目：
   ```bash
   ./gradlew fatJar
   ```
   产物：`build/libs/Lithium-0.1.0-all.jar`

2. 安装扩展：
   ```bash
   npm i -g @vscode/vsce
   cd vscode-extension
   vsce package
   code --install-extension lithium-lang-0.1.0.vsix
   ```

3. 打开任意 `.lit` 文件，右键 → **Lithium: 运行当前文件**。

## 配置

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `lithium.javaCommand` | `java` | Java 可执行文件路径 |
| `lithium.jarPath` | `""` | Lithium fat JAR 路径，留空则自动查找 `build/libs/*-all.jar` |