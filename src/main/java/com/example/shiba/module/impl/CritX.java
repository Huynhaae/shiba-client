package com.example.shiba.module.impl;

import com.example.shiba.module.Module;
import com.example.shiba.module.Category;
import com.example.shiba.module.settings.ModeSetting;
import com.example.shiba.module.settings.NumberSetting;
import com.example.shiba.module.settings.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;

public class CritX extends Module {
    // Settings – public để GUI có thể truy cập
    public final ModeSetting mode = new ModeSetting("Mode", "Timing", "Timing", "Standard", "Blatant");
    public final NumberSetting delay = new NumberSetting("Delay (ms)", 0, 500, 100, 10);
    public final BooleanSetting onlyOnGround = new BooleanSetting("OnlyOnGround", true);
    public final BooleanSetting autoJump = new BooleanSetting("AutoJump", true);
    public final NumberSetting range = new NumberSetting("Range", 3.0, 6.0, 4.5, 0.1);

    private long lastAttackTime = 0;

    public CritX() {
        super("CritX", "Critical hits với nhiều chế độ bypass", Category.COMBAT);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        ClientPlayerEntity player = mc.player;

        if (!mc.options.attackKey.isPressed()) return;
        if (mc.targetedEntity == null) return;
        if (player.distanceTo(mc.targetedEntity) > range.getValue()) return;

        long now = System.currentTimeMillis();
        if (now - lastAttackTime < delay.getValue()) return;

        String modeName = mode.getValue();
        boolean shouldCrit = false;

        switch (modeName) {
            case "Timing":
                if (player.getVelocity().y < 0 && !player.isOnGround() && isAboutToLand(mc)) {
                    shouldCrit = true;
                }
                break;

            case "Standard":
                if (autoJump.getValue() && player.isOnGround()) {
                    player.jump();
                }
                if (!player.isOnGround() && player.getVelocity().y < 0) {
                    shouldCrit = true;
                }
                break;

            case "Blatant":
                if (autoJump.getValue()) {
                    player.jump();
                }
                if (player.fallDistance > 0.1 || player.getVelocity().y < 0) {
                    shouldCrit = true;
                } else {
                    shouldCrit = true;
                }
                break;
        }

        if (onlyOnGround.getValue() && player.isOnGround()) {
            shouldCrit = false;
        }

        if (shouldCrit) {
            mc.interactionManager.attackEntity(player, mc.targetedEntity);
            player.swingHand(Hand.MAIN_HAND);
            lastAttackTime = now;
        }
    }

    private boolean isAboutToLand(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        if (player == null) return false;
        double y = player.getY();
        for (int i = 1; i <= 3; i++) {
            double checkY = y - i * 0.2;
            if (checkY < 0) break;
            if (!mc.world.isAir(player.getBlockPos().add(0, -i, 0))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        lastAttackTime = 0;
    }
}
