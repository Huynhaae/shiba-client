package com.example.shiba.mixin;

import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.impl.AimX;
import com.example.shiba.module.impl.MaceX;
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

        float[] angles = null;

        // Kiểm tra AimX Silent
        AimX aimX = ModuleManager.AIMX;
        if (aimX != null && aimX.isEnabled() && aimX.getMode().equals("Silent") && mc.options.attackKey.isPressed()) {
            angles = aimX.getAimAngles(mc);
        }

        // Kiểm tra MaceX Silent
        if (angles == null) {
            MaceX maceX = ModuleManager.MACEX;
            if (maceX != null && maceX.isEnabled() && maceX.isSilentAimEnabled()) {
                angles = maceX.getAimAngles(mc);
            }
        }

        if (angles == null) return packet;

        return new PlayerMoveC2SPacket.LookAndOnGround(
                angles[0],
                angles[1],
                mc.player.isOnGround()
        );
    }
}
