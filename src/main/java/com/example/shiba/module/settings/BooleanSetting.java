package com.example.shiba.module.settings;

public class BooleanSetting extends Setting {
    private boolean value;

    public BooleanSetting(String name, boolean defaultValue) {
        super(name);
        this.value = defaultValue;
    }

    public boolean getValue() { return value; }
    public void setValue(boolean value) { this.value = value; }
}
