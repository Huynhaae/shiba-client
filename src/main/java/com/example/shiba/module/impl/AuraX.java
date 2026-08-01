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
    private final ModeSetting mode = new ModeSetting("Mode", "Silent", "Silent", "Legit", "Blatant");
    private final NumberSetting cps = new NumberSetting("CPS", 1.0, 20.0, 10.0, 0.5);
    private final NumberSetting range = new NumberSetting("Range", 3.0, 8.0, 5.0, 0.1);
    private final BooleanSetting autoCrit = new BooleanSetting("AutoCrit", true);
    private final BooleanSetting silentRot = new BooleanSetting("SilentRotation", true);
    private final BooleanSetting onlyPlayers = new BooleanSetting("OnlyPlayers", true);
    private final BooleanSetting throughWalls = new BooleanSetting("ThroughWalls", false);

    private long lastAttackTime = 0;
    private LivingEntity target = null;

    public AuraX() {
        super("AuraX", "KillAura nâng cao với nhiều chế độ", Category.COMBAT);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        target = findTarget(mc);
        if (target == null) return;

        double dist = mc.player.distanceTo(target);
        if (dist > range.getValue()) return;

        long now = System.currentTimeMillis();
        long delay = (long) (1000 / cps.getValue());
        if (now - lastAttackTime < delay) return;

        if (mode.getValue().equals("Silent") || silentRot.getValue()) {
            rotateToTarget(mc, target);
        }

        if (autoCrit.getValue() && mc.player.isOnGround()) {
            mc.player.jump();
        }

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        lastAttackTime = now;
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
}
