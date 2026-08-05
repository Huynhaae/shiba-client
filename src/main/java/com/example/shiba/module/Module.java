package com.example.shiba.module;

import net.minecraft.client.MinecraftClient;

public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled = false;
    private int keybind = 0;

    protected MinecraftClient mc = MinecraftClient.getInstance();

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) onEnable();
            else onDisable();
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public int getKeybind() {
        return keybind;
    }

    public void setKeybind(int key) {
        this.keybind = key;
    }

    // Các method này có thể được override bởi subclass với protected
    protected void onEnable() {}
    protected void onDisable() {}
    public void onTick() {}

    // Xử lý keybind – sẽ được gọi từ ShibaClient
    public void tickKeybind(boolean pressed) {
        if (pressed) {
            toggle();
        }
    }
}
