package com.lithium.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * lit 项目资源访问器。
 *
 * 由于 lit 项目位于 src/main/resources/，Gradle 会把它原样打进 JAR 根目录：
 *   /lit.json
 *   /config/default.litc
 *   /src/hello.lit
 *
 * 因此读取时优先走 classpath（JAR 场景），其次走磁盘（IDE / 源码目录）。
 */
public final class LitProject {

    /** 磁盘上的项目根（相对工作目录）。 */
    public static final String DISK_ROOT = "src/main/resources/";

    private LitProject() {}

    /** 读取文本资源，relPath 例如 "lit.json"、"config/default.litc"、"src/hello.lit"。 */
    public static String readText(String relPath) throws IOException {
        String cleaned = relPath.startsWith("/") ? relPath.substring(1) : relPath;

        // 1) classpath：JAR 里的 /<cleaned>
        try (InputStream in = LitProject.class.getResourceAsStream("/" + cleaned)) {
            if (in != null) return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        // 2) 磁盘：src/main/resources/<cleaned>
        Path p = Path.of(DISK_ROOT + cleaned);
        if (Files.exists(p)) return Files.readString(p, StandardCharsets.UTF_8);

        // 3) 兜底：直接按工作目录相对路径找
        Path raw = Path.of(cleaned);
        if (Files.exists(raw)) return Files.readString(raw, StandardCharsets.UTF_8);

        throw new IOException("lit resource not found: " + relPath);
    }

    /** 读取二进制资源，relPath 例如 "logo.png"。 */
    public static byte[] readBytes(String relPath) throws IOException {
        String cleaned = relPath.startsWith("/") ? relPath.substring(1) : relPath;

        try (InputStream in = LitProject.class.getResourceAsStream("/" + cleaned)) {
            if (in != null) return in.readAllBytes();
        }
        Path p = Path.of(DISK_ROOT + cleaned);
        if (Files.exists(p)) return Files.readAllBytes(p);

        Path raw = Path.of(cleaned);
        if (Files.exists(raw)) return Files.readAllBytes(raw);

        throw new IOException("lit resource not found: " + relPath);
    }

    /** 资源是否存在。 */
    public static boolean exists(String relPath) {
        String cleaned = relPath.startsWith("/") ? relPath.substring(1) : relPath;
        if (LitProject.class.getResource("/" + cleaned) != null) return true;
        if (Files.exists(Path.of(DISK_ROOT + cleaned))) return true;
        return Files.exists(Path.of(cleaned));
    }
}