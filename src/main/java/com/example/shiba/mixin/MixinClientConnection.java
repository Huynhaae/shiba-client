package com.example.shiba.mixin;

import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.impl.AimX;
import com.example.shiba.util.PacketBlocker;
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
        // Nếu đang bỏ qua, không xử lý (tránh loop)
        if (PacketBlocker.shouldIgnoreLookPackets()) return;

        // Chỉ xử lý packet PlayerMoveC2SPacket có thay đổi góc
        if (!(packet instanceof PlayerMoveC2SPacket)) return;
        PlayerMoveC2SPacket movePacket = (PlayerMoveC2SPacket) packet;
        if (!movePacket.changesLook()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        AimX aimX = ModuleManager.AIMX;
        if (aimX == null || !aimX.isEnabled()) return;

        // Chỉ áp dụng khi đang giữ chuột trái (tấn công)
        if (!mc.options.attackKey.isPressed()) return;

        // Lấy góc aim từ module
        float[] angles = aimX.getAimAngles(mc);
        if (angles == null) return;

        // Hủy packet gốc, gửi packet mới với góc đã sửa
        ci.cancel();
        PlayerMoveC2SPacket newPacket = new PlayerMoveC2SPacket.LookAndOnGround(
                angles[0],
                angles[1],
                mc.player.isOnGround()
        );

        // Bỏ qua mixin để tránh loop
        PacketBlocker.setIgnoreLookPackets(true);
        ((ClientConnection) (Object) this).send(newPacket);
        PacketBlocker.setIgnoreLookPackets(false);
    }
}
