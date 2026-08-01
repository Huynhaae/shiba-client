package com.example.shiba.module.impl;

import com.example.shiba.module.Module;
import com.example.shiba.module.Category;  // import Category
import com.example.shiba.module.settings.NumberSetting;

public class HitboxBV extends Module {
    public final NumberSetting width = new NumberSetting("Width", 0.0, 10.0, 0.3, 0.05);
    public final NumberSetting height = new NumberSetting("Height", 0.0, 10.0, 0.3, 0.05);

   public HitboxBV() {
    super("HitboxBV", "Mở rộng hitbox (có slider)", Category.COMBAT);
    System.out.println("HitboxBV initialized!"); // <-- thêm dòng này
}

    public float getWidth() {
        return isEnabled() ? (float) width.getValue() : 0f;
    }

    public float getHeight() {
        return isEnabled() ? (float) height.getValue() : 0f;
    }
}
