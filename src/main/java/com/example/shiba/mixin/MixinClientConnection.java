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
        if (!(packet instanceof PlayerMoveC2SPacket)) return packet;
        PlayerMoveC2SPacket movePacket = (PlayerMoveC2SPacket) packet;
        if (!movePacket.changesLook()) return packet;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return packet;

        AimX aimX = ModuleManager.AIMX;
        if (aimX == null || !aimX.isEnabled()) return packet;

        if (!aimX.getMode().equals("Silent")) return packet;
        if (!mc.options.attackKey.isPressed()) return packet;

        float[] angles = aimX.getAimAngles(mc);
        if (angles == null) return packet;

        // Thay thế packet bằng packet mới, không set góc player
        return new PlayerMoveC2SPacket.LookAndOnGround(
                angles[0],
                angles[1],
                mc.player.isOnGround()
        );
    }
}
