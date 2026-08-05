package com.example.shiba.mixin;

import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.impl.AimX;
import com.example.shiba.module.impl.MaceX;
import com.example.shiba.util.SilentPacketHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {

    private long lastPacketTime = 0;

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = false)
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || target == null) return;

        // === 1. AimX ===
        AimX aimX = ModuleManager.AIMX;
        if (aimX != null && aimX.isEnabled()) {
            String mode = aimX.getMode();
            if (!mode.equals("None")) {
                LivingEntity aimTarget = aimX.getTarget();
                if (aimTarget != null && aimTarget == target) {
                    float[] angles = aimX.getAimAngles(mc);
                    if (angles != null) {
                        if (mode.equals("Silent")) {
                            // Silent: gửi packet giả (không set góc player)
                            sendFakeLookPacket(mc, angles[0], angles[1]);
                        } else {
                            // Normal/Legit: set góc player (gửi kèm packet tấn công)
                            float yaw = angles[0];
                            float pitch = angles[1];
                            if (mode.equals("Legit")) {
                                float currentYaw = mc.player.getYaw();
                                float currentPitch = mc.player.getPitch();
                                float yawDiff = MathHelper.wrapDegrees(yaw - currentYaw);
                                float pitchDiff = pitch - currentPitch;
                                float maxSpeed = (float) aimX.legitSpeed.getValue();
                                if (Math.abs(yawDiff) > maxSpeed) {
                                    yaw = currentYaw + Math.signum(yawDiff) * maxSpeed;
                                }
                                if (Math.abs(pitchDiff) > maxSpeed / 2) {
                                    pitch = currentPitch + Math.signum(pitchDiff) * maxSpeed / 2;
                                }
                            }
                            mc.player.setYaw(yaw);
                            mc.player.setPitch(pitch);
                        }
                    }
                }
            }
        }

        // === 2. MaceX: swap mace trước khi đánh ===
        MaceX maceX = ModuleManager.MACEX;
        if (maceX != null && maceX.isEnabled() && maceX.autoAttack.getValue()) {
            if (mc.player.fallDistance > maceX.fallDistance.getValue() && maceX.isAboutToLand(mc)) {
                if (!maceX.isHoldingMace(mc.player)) {
                    int slot = maceX.findMaceSlot(mc.player);
                    if (slot != -1) {
                        maceX.swapToSlot(mc.player, slot);
                    }
                }
            }
        }
    }

    private void sendFakeLookPacket(MinecraftClient mc, float yaw, float pitch) {
        // Giới hạn tần suất gửi packet để tránh giật
        long now = System.currentTimeMillis();
        if (now - lastPacketTime < 50) return;
        lastPacketTime = now;

        PlayerMoveC2SPacket packet = new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround());
        SilentPacketHelper.setSilentPacket(true);
        mc.player.networkHandler.sendPacket(packet);
        SilentPacketHelper.setSilentPacket(false);
    }
}
