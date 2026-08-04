package com.example.shiba.mixin;

import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.impl.AimX;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientConnection {

    @Inject(method = "send", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (!(packet instanceof PlayerMoveC2SPacket)) return;
        PlayerMoveC2SPacket movePacket = (PlayerMoveC2SPacket) packet;
        if (!movePacket.changesLook()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        AimX aimX = ModuleManager.AIMX;
        if (aimX == null || !aimX.isEnabled()) return;

        // Chỉ áp dụng khi đang Silent và giữ chuột trái
        if (!aimX.getMode().equals("Silent")) return;
        if (!mc.options.attackKey.isPressed()) return;

        float[] angles = aimX.getAimAngles(mc);
        if (angles == null) return;

        // Hủy packet gốc
        ci.cancel();

        // Gửi packet mới với góc đã sửa (KHÔNG set yaw/pitch)
        PlayerMoveC2SPacket newPacket = new PlayerMoveC2SPacket.LookAndOnGround(
                angles[0],
                angles[1],
                mc.player.isOnGround()
        );

        // Gửi trực tiếp (bỏ qua mixin để tránh loop)
        // Sử dụng sendPacket của network handler hoặc gọi lại send nhưng với flag
        // Cách an toàn: gửi qua network handler
        mc.player.networkHandler.sendPacket(newPacket);
    }
}
