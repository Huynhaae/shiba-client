package com.example.shiba.module.impl;

import com.example.shiba.module.Category;
import com.example.shiba.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class TriggerBot extends Module {

    public double fov = 15.0;
    public double range = 3.0;
    public boolean critEnabled = false;

    private int cooldownTicks = 0;

    public TriggerBot() {
        super("TriggerBot", "Tu dong danh khi crosshair nam trong FOV va tam.", Category.COMBAT);
    }

    @Override
    protected void onEnable() {
        cooldownTicks = 0;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.cameraEntity == null) return;
        if (mc.currentScreen != null) return;

        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }

        Entity target = findTarget(mc);
        if (target == null) return;

        if (critEnabled) {
            boolean falling = mc.player.fallDistance > 0.0F && mc.player.getVelocity().y < 0.0;
            if (!falling) {
                if (mc.player.isOnGround()) {
                    mc.player.jump();
                }
                return;
            }
        }

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        cooldownTicks = 2;
    }

    private Entity findTarget(MinecraftClient mc) {
        Vec3d eyePos = mc.cameraEntity.getEyePos();
        Vec3d lookVec = mc.cameraEntity.getRotationVec(1.0F).normalize();

        Entity closest = null;
        double closestAngle = fov / 2.0;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isDead() || living.getHealth() <= 0) continue;

            double distance = eyePos.distanceTo(entity.getBoundingBox().getCenter());
            if (distance > range) continue;

            Vec3d toEntity = entity.getBoundingBox().getCenter().subtract(eyePos).normalize();
            double dot = Math.max(-1.0, Math.min(1.0, lookVec.dotProduct(toEntity)));
            double angleDeg = Math.toDegrees(Math.acos(dot));

            if (angleDeg < closestAngle) {
                closestAngle = angleDeg;
                closest = entity;
            }
        }

        return closest;
    }
}
