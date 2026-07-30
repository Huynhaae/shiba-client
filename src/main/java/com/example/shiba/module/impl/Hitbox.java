package com.example.shiba.module.impl;

import com.example.shiba.module.Category;
import com.example.shiba.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class Hitbox extends Module {

    public static final double MIN_EXPAND = 0.0;
    public static final double MAX_EXPAND = 20.0;

    private double expand = 0.1;

    public boolean renderOutline = true;

    public Hitbox() {
        super("Hitbox", "Mở rộng hitbox của entity để dễ trúng đòn hơn.", Category.COMBAT);
    }

    public double getExpand() {
        return expand;
    }

    public void setExpand(double value) {
        this.expand = Math.max(MIN_EXPAND, Math.min(MAX_EXPAND, value));
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    @Override
    public void onTick() {
    }

    public Box getExpandedBox(Entity entity) {
        return entity.getBoundingBox().expand(expand);
    }

    public Entity findExpandedTarget(double reach) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.cameraEntity == null) return null;

        Vec3d eyePos = mc.cameraEntity.getEyePos();
        Vec3d lookVec = mc.cameraEntity.getRotationVec(1.0F);
        Vec3d reachEnd = eyePos.add(lookVec.multiply(reach));

        List<Entity> candidates = mc.world.getOtherEntities(
                mc.player,
                mc.player.getBoundingBox().stretch(lookVec.multiply(reach)).expand(expand)
        );

        Entity closest = null;
        double closestDistance = reach;

        for (Entity candidate : candidates) {
            if (!(candidate instanceof LivingEntity living)) continue;
            if (living.isDead() || living.getHealth() <= 0) continue;

            Box box = getExpandedBox(candidate);
            var hit = box.raycast(eyePos, reachEnd);
            if (hit.isEmpty()) continue;

            double distance = eyePos.distanceTo(hit.get());
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = candidate;
            }
        }

        return closest;
    }

    public void renderExpandedBox(MatrixStack matrices, WorldRenderer worldRenderer, Entity target) {
        if (!isEnabled() || !renderOutline || target == null) return;
        // Hook thực tế nối vào ShibaClient.java qua WorldRenderEvents.AFTER_ENTITIES
    }
}
