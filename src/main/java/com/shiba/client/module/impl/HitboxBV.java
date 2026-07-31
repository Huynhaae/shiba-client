package com.shiba.client.module.impl;

import com.shiba.client.module.Module;
import com.shiba.client.module.ModuleCategory;
import com.shiba.client.module.settings.NumberSetting;

public class HitboxBV extends Module {
    public final NumberSetting width = new NumberSetting("Width", 0.0, 10.0, 0.3, 0.05);
    public final NumberSetting height = new NumberSetting("Height", 0.0, 10.0, 0.3, 0.05);

    public HitboxBV() {
        super("HitboxBV", "Mở rộng hitbox (có slider)", ModuleCategory.COMBAT);
        addSettings(width, height);
    }

    public float getWidth() {
        return isEnabled() ? width.getValue().floatValue() : 0f;
    }

    public float getHeight() {
        return isEnabled() ? height.getValue().floatValue() : 0f;
    }
}
