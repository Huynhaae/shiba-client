package com.example.shiba.module.impl;

import com.example.shiba.module.Module;
import com.example.shiba.module.Category;
import com.example.shiba.module.settings.NumberSetting;
import com.example.shiba.module.settings.ModeSetting;
import com.example.shiba.module.settings.BooleanSetting;
import com.example.shiba.mixin.MixinClientConnection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AuraX extends Module {
    // Settings
    private final ModeSetting mode = new ModeSetting("Mode", "Silent", "Silent", "Normal", "None");
    private final NumberSetting minCPS = new NumberSetting("MinCPS", 6.0, 12.0, 8.0, 0.5);
    private final NumberSetting maxCPS = new NumberSetting("MaxCPS", 10.0, 20.0, 14.0, 0.5);
    private final NumberSetting range = new NumberSetting("Range", 3.0, 8.0, 4.5, 0.1);
    private final BooleanSetting autoCrit = new BooleanSetting("AutoCrit", true);
    private final ModeSetting critMode = new ModeSetting("CritMode", "Timing", "Timing", "Standard");
    private final BooleanSetting onlyPlayers = new BooleanSetting("OnlyPlayers", true);
    private final BooleanSetting throughWalls = new BooleanSetting("ThroughWalls", false);

    private long lastAttackTime = 0;
    private LivingEntity target = null;
    private double currentCPS = 10.0;

    public AuraX() {
        super("AuraX", "KillAura với Timing Crit & Silent Rotation", Category.COMBAT);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        adjustCPS();
        target = findTarget(mc);
        if (target == null) return;

        double dist = mc.player.distanceTo(target);
        if (dist > range.getValue()) return;

        long now = System.currentTimeMillis();
        long delay = (long) (1000 / currentCPS);
        if (now - lastAttackTime < delay) return;

        String currentMode = mode.getValue();

        if (currentMode.equals("Silent")) {
            sendFakeRotation(mc, target);
        } else if (!currentMode.equals("None")) {
            rotateToTarget(mc, target);
        }

        boolean shouldAttack = false;
        if (autoCrit.getValue()) {
            if (critMode.getValue().equals("Timing")) {
                if (isAboutToLand(mc)) {
                    shouldAttack = true;
                }
            } else {
                if (mc.player.isOnGround()) {
                    shouldAttack = true;
                }
            }
        } else {
            shouldAttack = true;
        }

        if (shouldAttack) {
            attack(mc, target);
            lastAttackTime = now;
        }
    }

    private void attack(MinecraftClient mc, LivingEntity target) {
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void adjustCPS() {
        double min = minCPS.getValue();
        double max = maxCPS.getValue();
        double targetCPS = min + (Math.random() * (max - min));
        currentCPS += (targetCPS - currentCPS) * 0.1;
        currentCPS = Math.min(max, Math.max(min, currentCPS));
    }

    private LivingEntity findTarget(MinecraftClient mc) {
        World world = mc.world;
        ClientPlayerEntity player = mc.player;
        if (world == null || player == null) return null;

        double r = range.getValue();
        Box box = new Box(player.getX() - r, player.getY() - r, player.getZ() - r,
                          player.getX() + r, player.getY() + r, player.getZ() + r);

        List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class, box, e -> e != player);

        if (onlyPlayers.getValue()) {
            entities = entities.stream()
                    .filter(e -> e instanceof PlayerEntity)
                    .collect(Collectors.toList());
        } else {
            entities = entities.stream()
                    .filter(e -> e instanceof PlayerEntity || e instanceof MobEntity)
                    .collect(Collectors.toList());
        }

        if (!throughWalls.getValue()) {
            entities = entities.stream()
                    .filter(player::canSee)
                    .collect(Collectors.toList());
        }

        return entities.stream()
                .min(Comparator.comparingDouble(e -> e.distanceTo(player)))
                .orElse(null);
    }

    private void rotateToTarget(MinecraftClient mc, LivingEntity target) {
        Vec3d targetPos = target.getPos().add(0, target.getHeight() / 2, 0);
        Vec3d playerPos = mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);
        Vec3d diff = targetPos.subtract(playerPos);

        double yaw = Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90;
        double pitch = -Math.toDegrees(Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z)));
        pitch = MathHelper.clamp(pitch, -90, 90);

        mc.player.setYaw((float) yaw);
        mc.player.setPitch((float) pitch);
    }

    private void sendFakeRotation(MinecraftClient mc, LivingEntity target) {
        Vec3d targetPos = target.getPos().add(0, target.getHeight() / 2, 0);
        Vec3d playerPos = mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);
        Vec3d diff = targetPos.subtract(playerPos);

        float yaw = (float) (Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90);
        float pitch = (float) (-Math.toDegrees(Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z))));
        pitch = MathHelper.clamp(pitch, -90, 90);

        // Gửi packet với vị trí hiện tại và góc xoay
        PlayerMoveC2SPacket packet = new PlayerMoveC2SPacket(
            mc.player.getX(), mc.player.getY(), mc.player.getZ(),
            yaw, pitch,
            mc.player.isOnGround(),
            true, true
        );

        MixinClientConnection.ignoreLookPackets = true;
        mc.player.networkHandler.sendPacket(packet);
        MixinClientConnection.ignoreLookPackets = false;
    }

    private boolean isAboutToLand(MinecraftClient mc) {
        if (mc.player == null) return false;
        if (mc.player.getVelocity().y < 0) {
            Vec3d start = mc.player.getPos();
            Vec3d end = start.add(0, -2, 0);
            RaycastContext context = new RaycastContext(start, end,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    mc.player);
            BlockHitResult hit = mc.world.raycast(context);
            if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                double distance = start.y - hit.getPos().y;
                return distance < 0.3 && distance > 0.01 && mc.player.getVelocity().y < -0.1;
            }
        }
        return false;
    }

    public String getMode() {
        return mode.getValue();
    }
}
