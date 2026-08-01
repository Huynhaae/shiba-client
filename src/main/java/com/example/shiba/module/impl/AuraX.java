package com.example.shiba.module.impl;

import com.example.shiba.module.Module;
import com.example.shiba.module.Category;
import com.example.shiba.module.settings.NumberSetting;
import com.example.shiba.module.settings.ModeSetting;
import com.example.shiba.module.settings.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AuraX extends Module {
    // Settings
    private final ModeSetting mode = new ModeSetting("Mode", "Silent", "Silent", "Legit", "Blatant");
    private final NumberSetting minCPS = new NumberSetting("MinCPS", 6.0, 12.0, 8.0, 0.5);
    private final NumberSetting maxCPS = new NumberSetting("MaxCPS", 10.0, 20.0, 14.0, 0.5);
    private final NumberSetting range = new NumberSetting("Range", 3.0, 8.0, 4.5, 0.1);
    private final BooleanSetting autoCrit = new BooleanSetting("AutoCrit", true);
    private final ModeSetting critMode = new ModeSetting("CritMode", "Timing", "Timing", "Standard");
    private final BooleanSetting silentRot = new BooleanSetting("SilentRotation", true);
    private final BooleanSetting onlyPlayers = new BooleanSetting("OnlyPlayers", true);
    private final BooleanSetting throughWalls = new BooleanSetting("ThroughWalls", false);

    // Variables
    private long lastAttackTime = 0;
    private LivingEntity target = null;
    private double currentCPS = 10.0;
    private boolean wasOnGround = true;

    public AuraX() {
        super("AuraX", "KillAura nâng cao với nhiều chế độ", Category.COMBAT);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        // Tự động điều chỉnh CPS (tránh bị anticheat phát hiện)
        adjustCPS();

        target = findTarget(mc);
        if (target == null) return;

        double dist = mc.player.distanceTo(target);
        if (dist > range.getValue()) return;

        long now = System.currentTimeMillis();
        long delay = (long) (1000 / currentCPS);
        if (now - lastAttackTime < delay) return;

        // Silent Rotation – không cần nhìn vẫn đánh trúng
        if (mode.getValue().equals("Silent") || silentRot.getValue()) {
            silentRotateToTarget(mc, target);
        } else {
            rotateToTarget(mc, target);
        }

        // Auto Crit - chỉ đánh khi chạm đất (không vừa nhảy vừa đánh)
        if (autoCrit.getValue() && mc.player.isOnGround() && !wasOnGround) {
            // Vừa chạm đất → đánh crit
            attack(mc, target);
            lastAttackTime = now;
        } else if (!autoCrit.getValue()) {
            // Đánh bình thường
            attack(mc, target);
            lastAttackTime = now;
        } else if (critMode.getValue().equals("Timing") && mc.player.isOnGround()) {
            // Chế độ Timing: chỉ đánh khi vừa rơi xuống
            if (!wasOnGround) {
                attack(mc, target);
                lastAttackTime = now;
            }
        }

        // Cập nhật trạng thái
        wasOnGround = mc.player.isOnGround();
    }

    private void attack(MinecraftClient mc, LivingEntity target) {
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void adjustCPS() {
        // Random trong khoảng minCPS và maxCPS để tránh bị phát hiện
        double min = minCPS.getValue();
        double max = maxCPS.getValue();
        // Thay đổi CPS từ từ để trông tự nhiên
        double targetCPS = min + (Math.random() * (max - min));
        currentCPS += (targetCPS - currentCPS) * 0.1; // Làm mịn
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

    /**
     * Silent Rotation: Không gửi packet xoay người lên server
     * Chỉ thay đổi góc nhìn client-side
     */
    private void silentRotateToTarget(MinecraftClient mc, LivingEntity target) {
        Vec3d targetPos = target.getPos().add(0, target.getHeight() / 2, 0);
        Vec3d playerPos = mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);
        Vec3d diff = targetPos.subtract(playerPos);

        double yaw = Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90;
        double pitch = -Math.toDegrees(Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z)));
        pitch = MathHelper.clamp(pitch, -90, 90);

        // Chỉ set client-side, không gửi lên server
        mc.player.setYaw((float) yaw);
        mc.player.setPitch((float) pitch);

        // Quan trọng: Không gửi packet rotation
        // Nếu cần, bạn có thể mixin để ngăn chặn việc gửi packet
    }

    /**
     * Normal Rotation (dùng cho Legit/Blatant)
     */
    private void rotateToTarget(MinecraftClient mc, LivingEntity target) {
        Vec3d targetPos = target.getPos().add(0, target.getHeight() / 2, 0);
        Vec3d playerPos = mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);
        Vec3d diff = targetPos.subtract(playerPos);

        double yaw = Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90;
        double pitch = -Math.toDegrees(Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z)));
        pitch = MathHelper.clamp(pitch, -90, 90);

        mc.player.setYaw((float) yaw);
        mc.player.setPitch((float) pitch);

        // Gửi packet rotation nếu cần
        // (mặc định Minecraft sẽ tự động gửi khi setYaw/setPitch)
    }
}
