package com.example.shiba.config;

import com.example.shiba.module.Module;
import com.example.shiba.module.ModuleManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TYPE = new TypeToken<Map<String, Boolean>>() {}.getType();
    private static final Path FILE =
            FabricLoader.getInstance().getConfigDir().resolve("shiba.json");

    private ConfigManager() {}

    public static void load() {
        if (!Files.exists(FILE)) return;
        try (Reader reader = Files.newBufferedReader(FILE)) {
            Map<String, Boolean> data = GSON.fromJson(reader, TYPE);
            if (data == null) return;
            data.forEach((name, on) -> {
                Module m = ModuleManager.byName(name);
                if (m != null) m.setEnabled(on);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        Map<String, Boolean> data = new HashMap<>();
        for (Module m : ModuleManager.getModules()) {
            data.put(m.getName(), m.isEnabled());
        }
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                GSON.toJson(data, TYPE, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
