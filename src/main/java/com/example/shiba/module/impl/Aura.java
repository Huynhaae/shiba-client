package com.example.shiba.module.impl;

import com.example.shiba.module.Category;
import com.example.shiba.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class Aura extends Module {

    public double range = 4.0;
    public double smoothness = 0.35;
    public double attackDelayTicks = 4.0;

    private int cooldownTicks = 0;

    public Aura() {
        super("Aura", "Tu dong xoay va tan cong entity gan nhat.", Category.COMBAT);
    }

    @Override
    protected void onEnable() {
        cooldownTicks = 0;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return;

        Entity target = findNearest(mc);
        if (target == null) return;

        rotateToward(mc, target);

        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }

        double dist = mc.player.getEyePos().distanceTo(target.getBoundingBox().getCenter());
        if (dist <= range) {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            cooldownTicks = (int) Math.round(attackDelayTicks);
        }
    }

    private Entity findNearest(MinecraftClient mc) {
        Vec3d playerPos = mc.player.getPos();
        Entity closest = null;
        double closestDist = range;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isDead() || living.getHealth() <= 0) continue;

            double dist = playerPos.distanceTo(entity.getPos());
            if (dist < closestDist) {
                closestDist = dist;
                closest = entity;
            }
        }
        return closest;
    }

    private void rotateToward(MinecraftClient mc, Entity target) {
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d center = target.getBoundingBox().getCenter();

        double dx = center.x - eyePos.x;
        double dy = center.y - eyePos.y;
        double dz = center.z - eyePos.z;
        double distXZ = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dy, distXZ));

        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();

        float yawDiff = wrapDegrees(targetYaw - currentYaw);
        float pitchDiff = targetPitch - currentPitch;

        float factor = (float) Math.max(0.05, Math.min(1.0, smoothness));

        mc.player.setYaw(currentYaw + yawDiff * factor);
        mc.player.setPitch(currentPitch + pitchDiff * factor);
    }

    private float wrapDegrees(float deg) {
        deg = deg % 360.0F;
        if (deg >= 180.0F) deg -= 360.0F;
        if (deg < -180.0F) deg += 360.0F;
        return deg;
    }
}
