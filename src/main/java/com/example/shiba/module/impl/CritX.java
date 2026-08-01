package com.example.shiba.module.impl;

import com.example.shiba.module.Module;
import com.example.shiba.module.Category;
import com.example.shiba.module.settings.ModeSetting;
import com.example.shiba.module.settings.NumberSetting;
import com.example.shiba.module.settings.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class CritX extends Module {
    // Settings
    private final ModeSetting mode = new ModeSetting("Mode", "Timing", "Timing", "Standard", "Blatant");
    private final NumberSetting delay = new NumberSetting("Delay (ms)", 0, 500, 100, 10);
    private final BooleanSetting onlyOnGround = new BooleanSetting("OnlyOnGround", true);
    private final BooleanSetting autoJump = new BooleanSetting("AutoJump", true);
    private final NumberSetting range = new NumberSetting("Range", 3.0, 6.0, 4.5, 0.1);

    private long lastAttackTime = 0;

    public CritX() {
        super("CritX", "Critical hits với nhiều chế độ bypass", Category.COMBAT);
        addSettings(mode, delay, onlyOnGround, autoJump, range);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        ClientPlayerEntity player = mc.player;

        // Chỉ đánh khi đang nhấn chuột trái và có mục tiêu
        if (!mc.options.attackKey.isPressed()) return;
        if (mc.targetedEntity == null) return;
        if (player.distanceTo(mc.targetedEntity) > range.getValue()) return;

        long now = System.currentTimeMillis();
        if (now - lastAttackTime < delay.getValue()) return;

        String modeName = mode.getValue();
        boolean shouldCrit = false;

        switch (modeName) {
            case "Timing":
                // Chỉ crit khi sắp chạm đất (đang rơi và gần đất)
                if (player.getVelocity().y < 0 && !player.isOnGround()) {
                    if (isAboutToLand(mc)) {
                        shouldCrit = true;
                    }
                }
                break;

            case "Standard":
                // Standard: nhảy và đánh khi rơi
                if (autoJump.getValue() && player.isOnGround()) {
                    player.jump();
                }
                if (!player.isOnGround() && player.getVelocity().y < 0) {
                    shouldCrit = true;
                }
                break;

            case "Blatant":
                // Blatant: luôn crit, nhảy liên tục, đánh bất kể trạng thái
                if (autoJump.getValue()) {
                    player.jump();
                }
                // Crit khi đang rơi hoặc vừa nhảy
                if (player.fallDistance > 0.1 || player.getVelocity().y < 0) {
                    shouldCrit = true;
                } else {
                    // Vẫn đánh nhưng không crit để tránh bị kick
                    shouldCrit = true;
                }
                break;
        }

        // Điều kiện bổ sung: nếu đang trên mặt đất và yêu cầu chỉ đánh khi đứng, không crit
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
        // Dự đoán chạm đất: khoảng cách tới block dưới chân < 0.5
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
