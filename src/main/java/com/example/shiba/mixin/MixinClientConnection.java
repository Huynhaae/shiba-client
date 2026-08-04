package com.example.shiba.mixin;

import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.impl.AimX;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ClientConnection.class)
public class MixinClientConnection {

    @ModifyVariable(method = "send", at = @At("HEAD"), argsOnly = true)
    private Packet<?> modifyPacket(Packet<?> packet) {
        // Chỉ xử lý packet PlayerMoveC2SPacket
        if (!(packet instanceof PlayerMoveC2SPacket)) return packet;
        PlayerMoveC2SPacket movePacket = (PlayerMoveC2SPacket) packet;
        if (!movePacket.changesLook()) return packet;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return packet;

        AimX aimX = ModuleManager.AIMX;
        if (aimX == null || !aimX.isEnabled()) return packet;

        // Chỉ áp dụng khi đang giữ chuột trái (tấn công)
        if (!mc.options.attackKey.isPressed()) return packet;

        // Lấy góc aim từ module
        float[] angles = aimX.getAimAngles(mc);
        if (angles == null) return packet;

        // Tạo packet mới với góc đã sửa (giữ nguyên onGround)
        return new PlayerMoveC2SPacket.LookAndOnGround(
                angles[0],
                angles[1],
                mc.player.isOnGround()
        );
    }
}
