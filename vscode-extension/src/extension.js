// Lithium VSCode 扩展主入口
const vscode = require('vscode');
const cp = require('child_process');
const fs = require('fs');
const path = require('path');

/**
 * 找出 Lithium fat JAR 路径。
 * 优先级：
 *   1. 配置里的 lithium.jarPath
 *   2. 工作区 build/libs/*-all.jar
 *   3. 工作区 **/*-all.jar（最多找一层深度）
*/
function findJar() {
    const cfg = vscode.workspace.getConfiguration('lithium');
    const configured = cfg.get('jarPath', '');
    if (configured && fs.existsSync(configured)) return configured;

    const folders = vscode.workspace.workspaceFolders;
    if (!folders) return null;

    for (const folder of folders) {
        const libDir = path.join(folder.uri.fsPath, 'build', 'libs');
        if (fs.existsSync(libDir)) {
            const files = fs.readdirSync(libDir).filter(f => f.endsWith('-all.jar'));
            if (files.length > 0) return path.join(libDir, files[0]);
        }
    }
    return null;
}

function javaCommand() {
    const cfg = vscode.workspace.getConfiguration('lithium');
    return cfg.get('javaCommand', 'java');
}

function runLithium(args, cwd) {
    const jar = findJar();
    if (!jar) {
        vscode.window.showErrorMessage(
            '未找到 Lithium fat JAR。请先执行 ./gradlew fatJar，或在设置中填写 lithium.jarPath。'
        );
        return null;
    }
    const cmd = `${javaCommand()} -jar "${jar}" ${args}`;
    const channel = vscode.window.createOutputChannel('Lithium');
    channel.clear();
    channel.show(true);
    channel.appendLine(`> ${cmd}`);
    channel.appendLine('');

    const proc = cp.spawn(javaCommand(), ['-jar', jar, ...args], {
        cwd,
        shell: false
    });
    proc.stdout.on('data', d => channel.append(d.toString()));
    proc.stderr.on('data', d => channel.append(d.toString()));
    proc.on('close', code => {
        channel.appendLine('');
        channel.appendLine(`[进程退出，代码 ${code}]`);
    });
    return proc;
}

function activate(context) {
    context.subscriptions.push(
        vscode.commands.registerCommand('lithium.runFile', () => {
            const editor = vscode.window.activeTextEditor;
            if (!editor) {
                vscode.window.showWarningMessage('没有打开的文件');
                return;
            }
            const file = editor.document.uri.fsPath;
            const cwd = vscode.workspace.workspaceFolders
                ? vscode.workspace.workspaceFolders[0].uri.fsPath
                : path.dirname(file);
            runLithium(['run', file], cwd);
        }),

        vscode.commands.registerCommand('lithium.parseConfig', () => {
            const editor = vscode.window.activeTextEditor;
            if (!editor) {
                vscode.window.showWarningMessage('没有打开的文件');
                return;
            }
            const file = editor.document.uri.fsPath;
            const cwd = vscode.workspace.workspaceFolders
                ? vscode.workspace.workspaceFolders[0].uri.fsPath
                : path.dirname(file);
            runLithium(['config', file], cwd);
        }),

        vscode.commands.registerCommand('lithium.showRegistry', () => {
            const cwd = vscode.workspace.workspaceFolders
                ? vscode.workspace.workspaceFolders[0].uri.fsPath
                : process.cwd();
            runLithium(['registry'], cwd);
        })
    );

    // 状态栏提示
    const status = vscode.window.createStatusBarItem(vscode.StatusBarAlignment.Left, 100);
    status.text = '$(beaker) Lithium';
    status.tooltip = 'Lithium 语言扩展';
    status.command = 'lithium.runFile';
    status.show();
    context.subscriptions.push(status);

    console.log('Lithium extension activated');
}

function deactivate() {}

module.exports = { activate, deactivate };