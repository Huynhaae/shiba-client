package com.example.shiba.module.impl;

import com.example.shiba.module.Category;
import com.example.shiba.module.Module;

public class WTap extends Module {

    public int pendingResprintTicks = 0;

    public WTap() {
        super("W-Tap", "Tu dong ngat sprint khi danh de tang knockback.", Category.COMBAT);
    }

    public void markTap() {
        pendingResprintTicks = 1;
    }

    @Override
    public void onTick() {
        // xử lý resprint nằm ở ShibaClient tick loop
    }
}
