package com.example.shiba.mixin;

import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.impl.AimX;
import com.example.shiba.module.impl.MaceX;
import com.example.shiba.util.SilentPacketHelper;
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
        // Nếu đang gửi packet giả, bỏ qua
        if (SilentPacketHelper.isSilentPacket()) return packet;

        if (!(packet instanceof PlayerMoveC2SPacket)) return packet;
        PlayerMoveC2SPacket movePacket = (PlayerMoveC2SPacket) packet;
        if (!movePacket.changesLook()) return packet;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return packet;

        // Ưu tiên AimX
        AimX aimX = ModuleManager.AIMX;
        if (aimX != null && aimX.isEnabled() && aimX.getMode().equals("Silent")) {
            if (mc.options.attackKey.isPressed() && aimX.getTarget() != null) {
                float[] angles = aimX.getAimAngles(mc);
                if (angles != null) {
                    return new PlayerMoveC2SPacket.LookAndOnGround(
                            angles[0],
                            angles[1],
                            mc.player.isOnGround()
                    );
                }
            }
        }

        // Nếu AimX không bật, thử MaceX
        MaceX maceX = ModuleManager.MACEX;
        if (maceX != null && maceX.isEnabled() && maceX.silentAim.getValue()) {
            if (mc.options.attackKey.isPressed()) {
                float[] angles = maceX.getAimAngles(mc);
                if (angles != null) {
                    return new PlayerMoveC2SPacket.LookAndOnGround(
                            angles[0],
                            angles[1],
                            mc.player.isOnGround()
                    );
                }
            }
        }

        return packet;
    }
}
