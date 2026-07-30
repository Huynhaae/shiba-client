package com.example.shiba.module.impl;

import com.example.shiba.module.Category;
import com.example.shiba.module.Module;

public class Hitbox extends Module {
    public static float expand = 0.5f; 
    public static final float MIN_EXPAND = 0.1f;
    public static final float MAX_EXPAND = 2.0f;

    public Hitbox() {
        super("Hitbox", "Mở rộng vùng nhận diện.", Category.COMBAT);
    }
}
