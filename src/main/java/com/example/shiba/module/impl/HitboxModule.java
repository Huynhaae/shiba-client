package com.example.shiba.module.impl;

import com.example.shiba.module.Module;
import com.example.shiba.module.Category;

public class HitboxModule extends Module {
    // Biến static để mixin đọc được trạng thái bật/tắt
    public static boolean enabled = false;
    // Hệ số phóng đại (có thể để người dùng nhập qua menu)
    public static float scale = 2.0f;

    public HitboxModule() {
        super("Hitbox", "Tăng kích thước hitbox của entity", Category.COMBAT);
        this.setKey(0); // không gán phím mặc định
    }

    @Override
    public void onEnable() {
        enabled = true;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        enabled = false;
        super.onDisable();
    }

    // Có thể thêm phương thức để thay đổi scale từ menu
    public void setScale(float scale) {
        HitboxModule.scale = scale;
    }
}
