package com.example.shiba.module.settings;

public class KeybindSetting extends Setting {
    private int keyCode;

    public KeybindSetting(String name, int defaultValue) {
        super(name);
        this.keyCode = defaultValue;
    }

    public int getValue() { return keyCode; }
    public void setValue(int key) { this.keyCode = key; }
}
