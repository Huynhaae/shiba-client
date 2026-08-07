package com.example.shiba.module.impl;

import com.example.shiba.module.Module;
import com.example.shiba.module.Category;
import com.example.shiba.module.settings.BooleanSetting;
import com.example.shiba.module.settings.KeybindSetting;
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
    public final KeybindSetting activeKey = new KeybindSetting("ActiveKey", 0);
    public final NumberSetting speed = new NumberSetting("Speed (blocks/sec)", 0.5, 10.0, 2.0, 0.5);
    public final NumberSetting range = new NumberSetting("Range (blocks)", 1.0, 10.0, 4.0, 0.5);
    public final BooleanSetting autoLight = new BooleanSetting("AutoLight", true);
    public final BooleanSetting placeRail = new BooleanSetting("PlaceRail", true);
    public final BooleanSetting placeTntCart = new BooleanSetting("PlaceTntCart", true);

    private long lastActionTime = 0;
    private boolean wasPressed = false;

    public AutoCartX() {
        super("AutoCartX", "Tự động đặt đường ray, TNT cart và quẹt lửa", Category.COMBAT);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        ClientPlayerEntity player = mc.player;

        // Kiểm tra phím kích hoạt
        boolean isKeyPressed = activeKey.getValue() > 0 && isKeyPressed(activeKey.getValue());
        if (!isKeyPressed) {
            wasPressed = false;
            return;
        }

        // Kiểm tra vũ khí
        boolean isValidWeapon = false;
        String currentMode = mode.getValue();

        if (currentMode.equals("Both") || currentMode.equals("Bow")) {
            if (player.getMainHandStack().getItem() == Items.BOW) isValidWeapon = true;
        }
        if (currentMode.equals("Both") || currentMode.equals("Crossbow")) {
            if (player.getMainHandStack().getItem() == Items.CROSSBOW) isValidWeapon = true;
        }

        if (!isValidWeapon) return;

        // Chỉ hành động khi nhấn phím lần đầu
        if (wasPressed) return;
        wasPressed = true;

        // Giới hạn tốc độ
        long now = System.currentTimeMillis();
        long delay = (long) (1000.0 / speed.getValue());
        if (now - lastActionTime < delay) return;

        // Tìm vị trí đặt
        BlockPos placePos = getPlacePos(mc);
        if (placePos == null) return;

        System.out.println("[AutoCartX] Placing at: " + placePos);

        // 1. Đặt đường ray
        if (placeRail.getValue()) {
            placeBlock(mc, placePos);
        }

        // 2. Đặt TNT cart
        if (placeTntCart.getValue()) {
            BlockPos cartPos = placePos.up();
            if (mc.world.getBlockState(cartPos).isAir()) {
                placeItem(mc, cartPos, Items.TNT_MINECART);
            }
        }

        // 3. Quẹt lửa
        if (autoLight.getValue()) {
            Direction facing = player.getHorizontalFacing();
            BlockPos lightPos = placePos.offset(facing, 1);
            if (mc.world.getBlockState(lightPos).isAir()) {
                placeItem(mc, lightPos, Items.FLINT_AND_STEEL);
            }
        }

        lastActionTime = now;
    }

    private BlockPos getPlacePos(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
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

        if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
            BlockPos target = hit.getBlockPos();
            if (mc.world.getBlockState(target).isAir()) {
                return target;
            }
            return target.offset(hit.getSide());
        }

        BlockPos pos = player.getBlockPos();
        return pos.add((int) lookVec.x * (int) range.getValue(),
                       (int) lookVec.y * (int) range.getValue(),
                       (int) lookVec.z * (int) range.getValue());
    }

    private void placeBlock(MinecraftClient mc, BlockPos pos) {
        if (mc.player == null) return;
        if (!mc.world.getBlockState(pos).isAir()) return;

        int slot = findItemSlot(mc.player, Items.RAIL);
        if (slot == -1) {
            System.out.println("[AutoCartX] No rail in hotbar!");
            return;
        }

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;

        mc.interactionManager.interactBlock(
                mc.player,
                Hand.MAIN_HAND,
                new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false)
        );

        mc.player.getInventory().selectedSlot = prevSlot;
        System.out.println("[AutoCartX] Placed rail");
    }

    private void placeItem(MinecraftClient mc, BlockPos pos, Item item) {
        if (mc.player == null) return;
        if (!mc.world.getBlockState(pos).isAir()) return;

        int slot = findItemSlot(mc.player, item);
        if (slot == -1) {
            System.out.println("[AutoCartX] No " + item.getName().getString() + " in hotbar!");
            return;
        }

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;

        mc.interactionManager.interactBlock(
                mc.player,
                Hand.MAIN_HAND,
                new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false)
        );

        mc.player.getInventory().selectedSlot = prevSlot;
        System.out.println("[AutoCartX] Placed " + item.getName().getString());
    }

    private int findItemSlot(ClientPlayerEntity player, Item item) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).getItem() == item) return i;
        }
        return -1;
    }

    private boolean isKeyPressed(int keyCode) {
        long handle = MinecraftClient.getInstance().getWindow().getHandle();
        return org.lwjgl.glfw.GLFW.glfwGetKey(handle, keyCode) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        wasPressed = false;
        lastActionTime = 0;
        System.out.println("[AutoCartX] Enabled!");
    }
}
