package com.lithium.pack;

import com.lithium.runtime.LitProject;
import com.lithium.util.Json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PackMeta {
    public final String packLogo;
    public final List<String> library;

    public PackMeta(String packLogo, List<String> library) {
        this.packLogo = packLogo;
        this.library = library;
    }

    /** 从 classpath 或磁盘读取 lit.json。 */
    public static PackMeta loadDefault() {
        try {
            String json = LitProject.readText("lit.json");
            return fromJson(json);
        } catch (IOException e) {
            throw new RuntimeException("failed to load lit.json", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static PackMeta fromJson(String json) {
        Object o = Json.parse(json);
        if (!(o instanceof Map<?, ?> map))
            throw new IllegalArgumentException("lit.json: root must be object");

        String logo = map.get("pack_logo") == null
                ? null
                : String.valueOf(map.get("pack_logo"));

        // ---- library 字段：支持 null / [] / ["null"] / 缺省 ----
        Object raw = map.get("library");
        List<String> lib = new ArrayList<>();

        if (raw == null) {
            // "library": null  →  空列表
        } else if (raw instanceof List<?> list) {
            for (Object item : list) {
                if (item == null) continue;
                String s = String.valueOf(item);
                if (s.isBlank() || "null".equalsIgnoreCase(s)) continue;
                lib.add(s);
            }
        } else {
            throw new IllegalArgumentException("lit.json: 'library' must be an array or null");
        }

        return new PackMeta(logo, lib);
    }

    @Override
    public String toString() {
        return "PackMeta{logo=" + packLogo + ", library=" + library + "}";
    }
}