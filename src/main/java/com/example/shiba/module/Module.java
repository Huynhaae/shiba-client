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

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) onEnable();
            else onDisable();
        }
    }

    public void toggle() { setEnabled(!enabled); }

    public int getKeybind() { return keybind; }
    public void setKeybind(int key) { this.keybind = key; }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
}
