package com.example.shiba.mixin;

import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.impl.AimX;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = false)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        // Chỉ xử lý packet di chuyển và có thay đổi góc
        if (!(packet instanceof PlayerMoveC2SPacket)) return;
        PlayerMoveC2SPacket movePacket = (PlayerMoveC2SPacket) packet;
        if (!movePacket.changesLook()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        AimX aimX = ModuleManager.AIMX;
        if (aimX == null || !aimX.isEnabled()) return;

        // Chỉ áp dụng khi đang tấn công (giữ chuột trái) và có target
        if (!mc.options.attackKey.isPressed()) return;

        // Lấy góc aim từ module
        float[] angles = aimX.getAimAngles(mc);
        if (angles == null) return;

        // Tạo packet mới với góc đã sửa (giữ nguyên vị trí)
        PlayerMoveC2SPacket newPacket = new PlayerMoveC2SPacket.LookAndOnGround(
                angles[0],
                angles[1],
                mc.player.isOnGround()
        );

        // Thay thế packet gốc bằng packet đã sửa (không thể thay trực tiếp, nên gửi packet mới và hủy packet cũ)
        // Cách an toàn: hủy packet cũ và gửi packet mới
        ci.cancel();
        mc.player.networkHandler.sendPacket(newPacket);
    }
}
