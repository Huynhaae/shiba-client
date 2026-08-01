package com.example.shiba.module.impl;

import com.example.shiba.module.Module;
import com.example.shiba.module.Category;
import com.example.shiba.module.settings.NumberSetting;
import com.example.shiba.module.settings.ModeSetting;
import com.example.shiba.module.settings.BooleanSetting;

public class HitboxX extends Module {
    public final NumberSetting width = new NumberSetting("Width", 0.0, 3.0, 0.5, 0.05);
    public final NumberSetting height = new NumberSetting("Height", 0.0, 3.0, 0.3, 0.05);
    public final BooleanSetting silentAim = new BooleanSetting("SilentAim", true);
    public final NumberSetting aimRange = new NumberSetting("AimRange", 3.0, 6.0, 4.5, 0.1);

    public HitboxX() {
        super("HitboxX", "Hitbox + Silent Aim nhẹ", Category.COMBAT);
    }

    public float getWidth() {
        return isEnabled() ? width.getValue().floatValue() : 0f;
    }

    public float getHeight() {
        return isEnabled() ? height.getValue().floatValue() : 0f;
    }

    public boolean isSilentAim() {
        return isEnabled() && silentAim.getValue();
    }

    public double getAimRange() {
        return aimRange.getValue();
    }
}
