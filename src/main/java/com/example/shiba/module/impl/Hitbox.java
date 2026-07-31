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

    public double expand = 0.1;
    public boolean renderOutline = true;

    public Hitbox() {
        super("Hitbox", "biet hitbox no ra sao roi con hoi cach dung , ngu vcl.", Category.COMBAT);
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
                mc.player.getBoundingBox().stretch(lookVec.multiply(reach)).expand(1.0)
        );

        Entity closest = null;
        double closestDistance = reach;

        for (Entity candidate : candidates) {
            if (!(candidate instanceof LivingEntity living)) continue;
            if (living.isDead() || living.getHealth() <= 0) continue;

            Box box = getExpandedBox(candidate);
            var hit = box.raycast(eyePos, reachEnd);

            if (hit.isPresent()) {
                double distance = eyePos.distanceTo(hit.get());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closest = candidate;
                }
            }
        }

        return closest;
    }

    public void renderExpandedBox(MatrixStack matrices, net.minecraft.client.render.VertexConsumerProvider consumers,
                                   Entity entity, Vec3d camPos) {
        if (!renderOutline) return;

        Box box = getExpandedBox(entity).offset(-camPos.x, -camPos.y, -camPos.z);
        var buffer = consumers.getBuffer(RenderLayer.getLines());

        matrices.push();
        WorldRenderer.drawBox(
                matrices, buffer,
                box.minX, box.minY, box.minZ,
                box.maxX, box.maxY, box.maxZ,
                1.0F, 0.2F, 0.2F, 0.8F
        );
        matrices.pop();
    }
}
