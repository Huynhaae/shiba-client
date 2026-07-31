package com.example.shiba.module.impl;

import com.example.shiba.module.Category;
import com.example.shiba.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class WTap extends Module {

    public int pendingResprintTicks = 0;

    public WTap() {
        super("W-Tap", "Tu dong ngat sprint khi danh de tang knockback.", Category.COMBAT);
    }

    public void markTap() {
        pendingResprintTicks = 1;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§d[W-Tap] §fTriggered"), true);
        }
    }

    @Override
    public void onTick() {
    }
}
