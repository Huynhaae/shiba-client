package com.example.shiba.module.impl;

import com.example.shiba.module.Module;
import com.example.shiba.module.Category;
import com.example.shiba.module.settings.BooleanSetting;
import com.example.shiba.module.settings.ModeSetting;
import com.example.shiba.module.settings.NumberSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class AutoCartX extends Module {
    public final ModeSetting mode = new ModeSetting("Mode", "Both", "Both", "Bow", "Crossbow");
    public final NumberSetting speed = new NumberSetting("Speed (blocks/sec)", 0.5, 10.0, 2.0, 0.5);
    public final NumberSetting range = new NumberSetting("Range (blocks)", 1.0, 10.0, 4.0, 0.5);
    public final BooleanSetting autoLight = new BooleanSetting("AutoLight", true);
    public final BooleanSetting placeRail = new BooleanSetting("PlaceRail", true);
    public final BooleanSetting placeTntCart = new BooleanSetting("PlaceTntCart", true);

    private long lastActionTime = 0;

    public AutoCartX() {
        super("AutoCartX", "Tự động đặt đường ray, TNT cart và quẹt lửa", Category.RENDER);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        ClientPlayerEntity player = mc.player;

        boolean isShooting = false;
        String currentMode = mode.getValue();
        if (currentMode.equals("Both") || currentMode.equals("Bow")) {
            if (mc.options.attackKey.isPressed() && player.getMainHandStack().getItem() == Items.BOW) {
                isShooting = true;
            }
        }
        if (currentMode.equals("Both") || currentMode.equals("Crossbow")) {
            if (mc.options.attackKey.isPressed() && player.getMainHandStack().getItem() == Items.CROSSBOW) {
                isShooting = true;
            }
        }

        if (!isShooting) return;

        long now = System.currentTimeMillis();
        long delay = (long) (1000.0 / speed.getValue());
        if (now - lastActionTime < delay) return;

        Vec3d start = player.getEyePos();
        Vec3d lookVec = player.getRotationVec(1.0f);
        Vec3d end = start.add(lookVec.multiply(range.getValue()));

        RaycastContext context = new RaycastContext(
                start, end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                player
        );
        BlockHitResult hit = mc.world.raycast(context);

        BlockPos placePos;
        if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
            placePos = hit.getBlockPos().offset(hit.getSide());
        } else {
            BlockPos target = player.getBlockPos().add(
                    (int) lookVec.x * (int) range.getValue(),
                    (int) lookVec.y * (int) range.getValue(),
                    (int) lookVec.z * (int) range.getValue()
            );
            placePos = target;
        }

        if (placePos == null) return;

        // Đặt đường ray
        if (placeRail.getValue() && mc.world.getBlockState(placePos).isAir()) {
            placeBlock(mc, placePos);
        }

        // Đặt TNT cart
        if (placeTntCart.getValue()) {
            BlockPos cartPos = placePos.up();
            if (mc.world.getBlockState(cartPos).isAir()) {
                int slot = findItemSlot(player, Items.TNT_MINECART);
                if (slot != -1) {
                    int prevSlot = player.getInventory().selectedSlot;
                    swapToSlot(player, slot);
                    mc.interactionManager.interactBlock(
                            player, Hand.MAIN_HAND,
                            new BlockHitResult(Vec3d.ofCenter(cartPos), Direction.UP, cartPos, false)
                    );
                    swapToSlot(player, prevSlot);
                }
            }
        }

        // Quẹt lửa (chỉ với nỏ)
        if (autoLight.getValue() && (currentMode.equals("Crossbow") || currentMode.equals("Both"))) {
            if (player.getMainHandStack().getItem() == Items.CROSSBOW || currentMode.equals("Crossbow")) {
                int flintSlot = findItemSlot(player, Items.FLINT_AND_STEEL);
                if (flintSlot != -1) {
                    int prevSlot = player.getInventory().selectedSlot;
                    swapToSlot(player, flintSlot);
                    BlockPos lightPos = placePos.offset(player.getHorizontalFacing(), 1);
                    if (mc.world.getBlockState(lightPos).isAir()) {
                        mc.interactionManager.interactBlock(
                                player, Hand.MAIN_HAND,
                                new BlockHitResult(Vec3d.ofCenter(lightPos), Direction.UP, lightPos, false)
                        );
                    }
                    swapToSlot(player, prevSlot);
                }
            }
        }

        lastActionTime = now;
    }

    private void placeBlock(MinecraftClient mc, BlockPos pos) {
        int slot = findItemSlot(mc.player, Items.RAIL);
        if (slot != -1) {
            int prevSlot = mc.player.getInventory().selectedSlot;
            swapToSlot(mc.player, slot);
            mc.interactionManager.interactBlock(
                    mc.player, Hand.MAIN_HAND,
                    new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false)
            );
            swapToSlot(mc.player, prevSlot);
        }
    }

    private int findItemSlot(ClientPlayerEntity player, Item item) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).getItem() == item) return i;
        }
        return -1;
    }

    private void swapToSlot(ClientPlayerEntity player, int slot) {
        if (slot >= 0 && slot < 9) {
            player.getInventory().selectedSlot = slot;
        }
    }
}
