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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AimX extends Module {
    // Settings
    private final ModeSetting mode = new ModeSetting("Mode", "Normal", "Normal", "Legit", "Silent");
    private final NumberSetting range = new NumberSetting("Range", 3.0, 8.0, 5.0, 0.1);
    private final NumberSetting fov = new NumberSetting("FOV", 30.0, 360.0, 180.0, 1.0);
    private final BooleanSetting onlyPlayers = new BooleanSetting("OnlyPlayers", true);
    private final BooleanSetting throughWalls = new BooleanSetting("ThroughWalls", false);
    private final NumberSetting legitSpeed = new NumberSetting("LegitSpeed", 1.0, 20.0, 8.0, 0.5);

    private LivingEntity target = null;
    private float lastYaw = 0, lastPitch = 0;
    private boolean shouldAim = false;

    public AimX() {
        super("AimX", "Tự động ngắm mục tiêu (Silent với mixin packet)", Category.COMBAT);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        target = findTarget(mc);
    }

    public LivingEntity getTarget() {
        return target;
    }

    public String getMode() {
        return mode.getValue();
    }

    /**
     * Trả về góc cần xoay (yaw, pitch) để nhắm vào target.
     * Nếu không có target hoặc mode None, trả về null.
     */
    public float[] getAimAngles(MinecraftClient mc) {
        if (target == null || mc.player == null) return null;
        String currentMode = mode.getValue();
        if (currentMode.equals("None")) return null;

        Vec3d targetPos = target.getPos().add(0, target.getHeight() / 2, 0);
        Vec3d playerPos = mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);
        Vec3d diff = targetPos.subtract(playerPos);

        float yaw = (float) (Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90);
        float pitch = (float) (-Math.toDegrees(Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z))));
        pitch = MathHelper.clamp(pitch, -90, 90);

        if (currentMode.equals("Legit")) {
            float currentYaw = mc.player.getYaw();
            float currentPitch = mc.player.getPitch();
            float yawDiff = MathHelper.wrapDegrees(yaw - currentYaw);
            float pitchDiff = pitch - currentPitch;
            float maxSpeed = (float) legitSpeed.getValue();
            if (Math.abs(yawDiff) > maxSpeed) {
                yaw = currentYaw + Math.signum(yawDiff) * maxSpeed;
            }
            if (Math.abs(pitchDiff) > maxSpeed / 2) {
                pitch = currentPitch + Math.signum(pitchDiff) * maxSpeed / 2;
            }
        }

        return new float[]{yaw, pitch};
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

        String currentMode = mode.getValue();
        if (!currentMode.equals("Silent")) {
            float yaw = player.getYaw();
            float pitch = player.getPitch();
            double fovLimit = fov.getValue();
            entities = entities.stream()
                    .filter(e -> {
                        Vec3d targetPos = e.getPos().add(0, e.getHeight() / 2, 0);
                        Vec3d playerPos = player.getPos().add(0, player.getEyeHeight(player.getPose()), 0);
                        Vec3d diff = targetPos.subtract(playerPos);
                        float yawTo = (float) (Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90);
                        float pitchTo = (float) (-Math.toDegrees(Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z))));
                        float yawDiff = MathHelper.wrapDegrees(yawTo - yaw);
                        float pitchDiff = pitchTo - pitch;
                        double angle = Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
                        return angle <= fovLimit;
                    })
                    .collect(Collectors.toList());
        }

        return entities.stream()
                .min(Comparator.comparingDouble(e -> e.distanceTo(player)))
                .orElse(null);
    }
}
