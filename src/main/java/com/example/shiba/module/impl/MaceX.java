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
import net.minecraft.entity.EquipmentSlot;
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
    private long lastAttackTime = 0;

    public MaceX() {
        super("MaceX", "Auto Mace + Swap elytra/chestplate", Category.COMBAT);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        ClientPlayerEntity player = mc.player;

        // Keybind: đổi elytra thành chestplate và ngược lại
        if (swapKey.getValue() > 0 && isKeyPressed(swapKey.getValue())) {
            swapElytraChestplate(mc);
        }

        // AutoAttack: chỉ khi rơi và sắp chạm đất
        if (autoAttack.getValue() && player.fallDistance > fallDistance.getValue() && isAboutToLand(mc)) {
            LivingEntity target = getTarget(mc);
            if (target != null && player.distanceTo(target) <= attackRange.getValue()) {
                // Nếu chưa cầm mace, đổi sang mace (hoán đổi item trong hotbar)
                if (!isHoldingMace(player)) {
                    int slot = findMaceSlot(player);
                    if (slot != -1) {
                        if (previousSlot == -1) previousSlot = player.getInventory().selectedSlot;
                        swapItemSlots(player, slot, player.getInventory().selectedSlot);
                    } else {
                        return; // Không có mace
                    }
                }
                // Đánh
                if (silentAim.getValue()) {
                    sendFakeRotation(mc, target);
                }
                mc.interactionManager.attackEntity(player, target);
                player.swingHand(Hand.MAIN_HAND);
                player.fallDistance = 0;
                lastAttackTime = System.currentTimeMillis();

                // Đổi lại slot cũ nếu có
                if (autoSwap.getValue() && previousSlot != -1) {
                    int maceSlot = player.getInventory().selectedSlot;
                    swapItemSlots(player, maceSlot, previousSlot);
                    previousSlot = -1;
                }
            }
        }
    }

    // === Swap elytra ↔ chestplate ===
    private void swapElytraChestplate(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        if (player == null) return;

        ItemStack chestSlot = player.getInventory().armor.get(2); // slot chestplate (index 2)

        if (chestSlot.getItem() == Items.ELYTRA) {
            // Đang mặc elytra → tìm chestplate để mặc
            int chestplateSlot = findItemSlot(player, Items.NETHERITE_CHESTPLATE);
            if (chestplateSlot == -1) chestplateSlot = findItemSlot(player, Items.DIAMOND_CHESTPLATE);
            if (chestplateSlot == -1) chestplateSlot = findItemSlot(player, Items.IRON_CHESTPLATE);
            if (chestplateSlot == -1) chestplateSlot = findItemSlot(player, Items.GOLDEN_CHESTPLATE);
            if (chestplateSlot == -1) chestplateSlot = findItemSlot(player, Items.CHAINMAIL_CHESTPLATE);
            if (chestplateSlot == -1) chestplateSlot = findItemSlot(player, Items.LEATHER_CHESTPLATE);
            if (chestplateSlot != -1) {
                // Lấy chestplate ra, bỏ elytra vào slot đó
                ItemStack chestplate = player.getInventory().getStack(chestplateSlot);
                player.getInventory().armor.set(2, chestplate);
                player.getInventory().setStack(chestplateSlot, new ItemStack(Items.ELYTRA));
                // Cập nhật equipment (client-side)
                player.sendEquipmentChanges();
            }
        } else {
            // Đang mặc chestplate → tìm elytra để mặc
            int elytraSlot = findItemSlot(player, Items.ELYTRA);
            if (elytraSlot != -1) {
                ItemStack elytra = player.getInventory().getStack(elytraSlot);
                player.getInventory().armor.set(2, elytra);
                player.getInventory().setStack(elytraSlot, chestSlot);
                player.sendEquipmentChanges();
            }
        }
    }

    // Tìm slot chứa item trong hotbar + inventory (ưu tiên hotbar)
    private int findItemSlot(ClientPlayerEntity player, net.minecraft.item.Item item) {
        // Ưu tiên hotbar (0-8)
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).getItem() == item) {
                return i;
            }
        }
        // Nếu không có trong hotbar, tìm trong toàn bộ inventory (9-35)
        for (int i = 9; i < 36; i++) {
            if (player.getInventory().getStack(i).getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    // Hoán đổi item giữa hai slot
    private void swapItemSlots(ClientPlayerEntity player, int slot1, int slot2) {
        if (slot1 == slot2) return;
        ItemStack stack1 = player.getInventory().getStack(slot1);
        ItemStack stack2 = player.getInventory().getStack(slot2);
        player.getInventory().setStack(slot1, stack2);
        player.getInventory().setStack(slot2, stack1);
        // Cập nhật selected slot nếu cần
        if (player.getInventory().selectedSlot == slot1) {
            player.getInventory().selectedSlot = slot2;
        } else if (player.getInventory().selectedSlot == slot2) {
            player.getInventory().selectedSlot = slot1;
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

    private LivingEntity getTarget(MinecraftClient mc) {
        HitResult hit = mc.crosshairTarget;
        if (hit != null && hit.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hit;
            if (entityHit.getEntity() instanceof LivingEntity) {
                return (LivingEntity) entityHit.getEntity();
            }
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
    }

    @Override
    public void onDisable() {
        super.onDisable();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (previousSlot != -1 && mc.player != null) {
            if (isHoldingMace(mc.player)) {
                int currentSlot = mc.player.getInventory().selectedSlot;
                swapItemSlots(mc.player, currentSlot, previousSlot);
            }
            previousSlot = -1;
        }
    }
}
