package com.example.shiba.module.impl;

import com.example.shiba.module.Module;
import com.example.shiba.module.Category;
import com.example.shiba.module.settings.BooleanSetting;
import com.example.shiba.module.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MaceX extends Module {
    private final BooleanSetting autoSwap = new BooleanSetting("AutoSwap", true);
    private final BooleanSetting autoAttack = new BooleanSetting("AutoAttack", true);
    private final NumberSetting fallDistance = new NumberSetting("FallDist", 1.0, 10.0, 3.0, 0.5);
    private final BooleanSetting silentAim = new BooleanSetting("SilentAim", true);
    private final NumberSetting attackRange = new NumberSetting("Range", 3.0, 6.0, 4.5, 0.1);

    private int previousSlot = -1;
    private boolean isUsingMace = false;
    private long lastAttackTime = 0;

    public MaceX() {
        super("MaceX", "Auto Mace - Swap, tấn công khi rơi, Silent Aim", Category.COMBAT);
        addSettings(autoSwap, autoAttack, fallDistance, silentAim, attackRange);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        ClientPlayerEntity player = mc.player;

        // Auto Attack khi rơi đủ khoảng cách và có mục tiêu
        if (autoAttack.getValue() && player.fallDistance >= fallDistance.getValue()) {
            if (player.getMainHandStack().getItem() instanceof MaceItem) {
                LivingEntity target = getTarget(mc);
                if (target != null && player.distanceTo(target) <= attackRange.getValue()) {
                    // Silent Aim nếu bật
                    if (silentAim.getValue()) {
                        // Silent aim sẽ được xử lý qua mixin, không set góc ở đây
                    }
                    // Đánh
                    mc.interactionManager.attackEntity(player, target);
                    player.swingHand(Hand.MAIN_HAND);
                    // Reset fall distance để tránh spam
                    player.fallDistance = 0;
                    lastAttackTime = System.currentTimeMillis();
                }
            }
        }

        // Auto Swap: tự động đổi sang mace khi tấn công và đổi về
        if (autoSwap.getValue() && mc.options.attackKey.isPressed()) {
            if (isHoldingMace(player)) {
                // Đã cầm mace, không làm gì
            } else {
                int slot = findMaceSlot(player);
                if (slot != -1) {
                    if (previousSlot == -1) previousSlot = player.getInventory().selectedSlot;
                    player.getInventory().selectedSlot = slot;
                    isUsingMace = true;
                }
            }
        } else if (isUsingMace && !mc.options.attackKey.isPressed()) {
            // Sau khi đánh xong, đổi về slot cũ
            if (previousSlot != -1) {
                player.getInventory().selectedSlot = previousSlot;
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

    private LivingEntity getTarget(MinecraftClient mc) {
        HitResult hit = mc.crosshairTarget;
        if (hit != null && hit.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hit;
            if (entityHit.getEntity() instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) entityHit.getEntity();
                if (target != mc.player) {
                    return target;
                }
            }
        }
        return null;
    }

    // Lấy góc aim cho silent (gọi từ mixin)
    public float[] getAimAngles(MinecraftClient mc) {
        if (!silentAim.getValue() || !isEnabled()) return null;
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

    public boolean isSilentAimEnabled() {
        return isEnabled() && silentAim.getValue();
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
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && previousSlot != -1) {
            mc.player.getInventory().selectedSlot = previousSlot;
            previousSlot = -1;
            isUsingMace = false;
        }
    }
}
