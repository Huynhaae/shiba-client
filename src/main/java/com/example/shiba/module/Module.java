package com.example.shiba.module;

public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled;

    public int keyCode = -1;
    private boolean keyWasDown = false;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean value) {
        if (this.enabled == value) return;
        this.enabled = value;
        if (value) onEnable(); else onDisable();
    }

    public void toggle() { setEnabled(!enabled); }

    public void tickKeybind(boolean isDown) {
        if (isDown && !keyWasDown) {
            toggle();
        }
        keyWasDown = isDown;
    }

    protected void onEnable() {}
    protected void onDisable() {}

    public void onTick() {}
}
