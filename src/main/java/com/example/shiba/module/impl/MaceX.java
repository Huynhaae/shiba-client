package com.example.shiba.module.impl;

import com.example.shiba.module.Module;
import com.example.shiba.module.Category;
import com.example.shiba.module.settings.BooleanSetting;
import com.example.shiba.module.settings.KeybindSetting;
import com.example.shiba.module.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

public class MaceX extends Module {
    private final BooleanSetting autoSwap = new BooleanSetting("AutoSwap", true);
    private final BooleanSetting autoAttack = new BooleanSetting("AutoAttack", true);
    private final KeybindSetting useKey = new KeybindSetting("UseKey", 0); // mặc định chưa set
    private final NumberSetting fallDistance = new NumberSetting("FallDist", 1.0, 10.0, 3.0, 0.5);
    private final BooleanSetting silentAim = new BooleanSetting("SilentAim", true);

    private int previousSlot = -1;
    private boolean isUsingMace = false;
    private long lastAttackTime = 0;

    public MaceX() {
        super("MaceX", "Auto Mace - Swap, tấn công khi rơi, use key", Category.COMBAT);
        addSettings(autoSwap, autoAttack, useKey, fallDistance, silentAim);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        ClientPlayerEntity player = mc.player;

        // Keybind: dùng mace như elytra (giữ phím)
        if (useKey.getValue() > 0 && mc.options.attackKey.isPressed()) {
            // Logic dùng mace để bay (tạm thời mô phỏng)
            if (player.getMainHandStack().getItem() instanceof MaceItem) {
                // Nếu đang cầm mace, dùng nó
                if (player.isFallFlying()) {
                    // Nếu đang bay, không làm gì
                }
            }
        }

        // Auto attack khi rơi và có mục tiêu
        if (autoAttack.getValue() && player.fallDistance > fallDistance.getValue()) {
            if (player.getMainHandStack().getItem() instanceof MaceItem) {
                // Tìm target
                LivingEntity target = getTarget(mc);
                if (target != null && player.distanceTo(target) <= 5.0) {
                    // Silent Aim nếu bật
                    if (silentAim.getValue()) {
                        aimAtTarget(mc, target);
                    }
                    // Đánh
                    mc.interactionManager.attackEntity(player, target);
                    player.swingHand(Hand.MAIN_HAND);
                    // Reset fall distance để không đánh liên tục
                    player.fallDistance = 0;
                    lastAttackTime = System.currentTimeMillis();
                }
            }
        }

        // Auto Swap: tự động đổi sang mace khi tấn công và đổi về sau
        if (autoSwap.getValue() && mc.options.attackKey.isPressed()) {
            if (isHoldingMace(player)) {
                // Nếu đã cầm mace, đánh bình thường
            } else {
                // Tìm slot có mace
                int slot = findMaceSlot(player);
                if (slot != -1) {
                    if (previousSlot == -1) previousSlot = player.getInventory().selectedSlot;
                    swapToSlot(player, slot);
                    isUsingMace = true;
                }
            }
        } else if (isUsingMace && !mc.options.attackKey.isPressed()) {
            // Sau khi đánh xong, đổi về slot cũ
            if (previousSlot != -1) {
                swapToSlot(player, previousSlot);
                previousSlot = -1;
                isUsingMace = false;
            }
        }
    }

    private boolean isHoldingMace(ClientPlayerEntity player) {
        return player.getMainHandStack().getItem() instanceof MaceItem;
    }

    private int findMaceSlot(ClientPlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() instanceof MaceItem) {
                return i;
            }
        }
        return -1;
    }

    private void swapToSlot(ClientPlayerEntity player, int slot) {
        player.getInventory().selectedSlot = slot;
    }

    private LivingEntity getTarget(MinecraftClient mc) {
        HitResult hit = mc.crosshairTarget;
        if (hit != null && hit.getType() == HitResult.Type.ENTITY) {
            net.minecraft.util.hit.EntityHitResult entityHit = (net.minecraft.util.hit.EntityHitResult) hit;
            if (entityHit.getEntity() instanceof LivingEntity) {
                return (LivingEntity) entityHit.getEntity();
            }
        }
        return null;
    }

    private void aimAtTarget(MinecraftClient mc, LivingEntity target) {
        Vec3d targetPos = target.getPos().add(0, target.getHeight() / 2, 0);
        Vec3d playerPos = mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);
        Vec3d diff = targetPos.subtract(playerPos);

        float yaw = (float) (Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90);
        float pitch = (float) (-Math.toDegrees(Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z))));
        pitch = MathHelper.clamp(pitch, -90, 90);

        // Set góc tạm thời
        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
    }

    // Hàm này gọi từ mixin để silent aim (nếu cần)
    public float[] getAimAngles(MinecraftClient mc) {
        if (!silentAim.getValue()) return null;
        LivingEntity target = getTarget(mc);
        if (target == null) return null;
        Vec3d targetPos = target.getPos().add(0, target.getHeight() / 2, 0);
        Vec3d playerPos = mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);
        Vec3d diff = targetPos.subtract(playerPos);
        float yaw = (float) (Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90);
        float pitch = (float) (-Math.toDegrees(Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z))));
        pitch = MathHelper.clamp(pitch, -90, 90);
        return new float[]{yaw, pitch};
    }

    @Override
    public void onEnable() {
        super.onEnable();
        previousSlot = -1;
        isUsingMace = false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (previousSlot != -1) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) {
                swapToSlot(mc.player, previousSlot);
            }
            previousSlot = -1;
            isUsingMace = false;
        }
    }
}
