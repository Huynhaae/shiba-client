package com.example.shiba.module.impl;

import com.example.shiba.module.Module;
import com.example.shiba.module.Category;
import com.example.shiba.module.settings.BooleanSetting;
import com.example.shiba.module.settings.KeybindSetting;
import com.example.shiba.module.settings.NumberSetting;
import com.example.shiba.util.SilentPacketHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MaceX extends Module {
    public final BooleanSetting autoSwap = new BooleanSetting("AutoSwap", true);
    public final BooleanSetting autoAttack = new BooleanSetting("AutoAttack", true);
    public final KeybindSetting swapKey = new KeybindSetting("SwapKey", 0);
    public final NumberSetting fallDistance = new NumberSetting("FallDist", 1.0, 10.0, 3.0, 0.5);
    public final BooleanSetting silentAim = new BooleanSetting("SilentAim", true);
    public final NumberSetting attackRange = new NumberSetting("AttackRange", 1.0, 6.0, 4.0, 0.1);

    private int previousSlot = -1;
    private boolean isUsingMace = false;
    private long lastAttackTime = 0;

    public MaceX() {
        super("MaceX", "Auto Mace - Swap, tấn công khi rơi, swap elytra", Category.COMBAT);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        ClientPlayerEntity player = mc.player;

        // Keybind swap elytra
        if (swapKey.getValue() > 0 && isKeyPressed(swapKey.getValue())) {
            swapElytraMace(mc);
        }

        // AutoAttack: chỉ khi rơi và sắp chạm đất
        if (autoAttack.getValue() && player.fallDistance > fallDistance.getValue() && isAboutToLand(mc)) {
            LivingEntity target = getTarget(mc);
            if (target != null && player.distanceTo(target) <= attackRange.getValue()) {
                // Nếu không cầm mace, đổi sang mace nếu có
                if (!isHoldingMace(player)) {
                    int slot = findMaceSlot(player);
                    if (slot != -1) {
                        if (previousSlot == -1) previousSlot = player.getInventory().selectedSlot;
                        swapToSlot(player, slot);
                        isUsingMace = true;
                    } else {
                        return; // Không có mace, không đánh
                    }
                }
                // Silent aim
                if (silentAim.getValue()) {
                    sendFakeRotation(mc, target);
                }
                mc.interactionManager.attackEntity(player, target);
                player.swingHand(Hand.MAIN_HAND);
                player.fallDistance = 0;
                lastAttackTime = System.currentTimeMillis();
            }
        }

        // AutoSwap: đổi sang mace khi tấn công (giữ nguyên)
        if (autoSwap.getValue() && mc.options.attackKey.isPressed()) {
            if (!isHoldingMace(player)) {
                int slot = findMaceSlot(player);
                if (slot != -1) {
                    if (previousSlot == -1) previousSlot = player.getInventory().selectedSlot;
                    swapToSlot(player, slot);
                    isUsingMace = true;
                }
            }
        } else if (isUsingMace && !mc.options.attackKey.isPressed()) {
            if (previousSlot != -1) {
                swapToSlot(player, previousSlot);
                previousSlot = -1;
                isUsingMace = false;
            }
        }
    }

    private boolean isAboutToLand(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        if (player == null) return false;
        BlockPos pos = player.getBlockPos();
        for (int i = 0; i < 3; i++) {
            BlockPos checkPos = pos.add(0, -i - 1, 0);
            if (!mc.world.isAir(checkPos)) {
                double yDist = player.getY() - (pos.getY() - i - 1);
                return yDist < 1.0 && player.getVelocity().y < -0.1;
            }
        }
        return false;
    }

    private boolean isKeyPressed(int keyCode) {
        long handle = MinecraftClient.getInstance().getWindow().getHandle();
        return org.lwjgl.glfw.GLFW.glfwGetKey(handle, keyCode) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
    }

    private void swapElytraMace(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        if (player == null) return;
        int maceSlot = -1, elytraSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() instanceof MaceItem) maceSlot = i;
            if (stack.getItem() == Items.ELYTRA) elytraSlot = i;
        }
        if (maceSlot != -1 && elytraSlot != -1) {
            ItemStack maceStack = player.getInventory().getStack(maceSlot);
            ItemStack elytraStack = player.getInventory().getStack(elytraSlot);
            player.getInventory().setStack(maceSlot, elytraStack);
            player.getInventory().setStack(elytraSlot, maceStack);
            if (player.getInventory().selectedSlot == maceSlot)
                swapToSlot(player, elytraSlot);
            else if (player.getInventory().selectedSlot == elytraSlot)
                swapToSlot(player, maceSlot);
        } else if (maceSlot != -1) {
            swapToSlot(player, maceSlot);
        } else if (elytraSlot != -1) {
            swapToSlot(player, elytraSlot);
        }
    }

    private void sendFakeRotation(MinecraftClient mc, LivingEntity target) {
        Vec3d targetPos = target.getPos().add(0, target.getHeight() / 2, 0);
        Vec3d playerPos = mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);
        Vec3d diff = targetPos.subtract(playerPos);
        float yaw = (float) (Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90);
        float pitch = (float) (-Math.toDegrees(Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z))));
        pitch = MathHelper.clamp(pitch, -90, 90);
        SilentPacketHelper.setSilentPacket(true);
        mc.player.networkHandler.sendPacket(
                new net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround())
        );
        SilentPacketHelper.setSilentPacket(false);
    }

    private boolean isHoldingMace(ClientPlayerEntity player) {
        return player.getMainHandStack().getItem() instanceof MaceItem;
    }

    private int findMaceSlot(ClientPlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).getItem() instanceof MaceItem) return i;
        }
        return -1;
    }

    private void swapToSlot(ClientPlayerEntity player, int slot) {
        player.getInventory().selectedSlot = slot;
    }

    private LivingEntity getTarget(MinecraftClient mc) {
        HitResult hit = mc.crosshairTarget;
        if (hit != null && hit.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hit;
            if (entityHit.getEntity() instanceof LivingEntity) return (LivingEntity) entityHit.getEntity();
        }
        return null;
    }

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
        MinecraftClient mc = MinecraftClient.getInstance();
        if (previousSlot != -1 && mc.player != null) {
            swapToSlot(mc.player, previousSlot);
            previousSlot = -1;
            isUsingMace = false;
        }
    }
}
