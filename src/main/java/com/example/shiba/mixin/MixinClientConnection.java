package com.example.shiba.mixin;

import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.impl.AuraX;
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
        AuraX aura = ModuleManager.AURAX;
        if (aura != null && aura.isEnabled() && aura.getMode().equals("Silent")) {
            if (packet instanceof PlayerMoveC2SPacket) {
                PlayerMoveC2SPacket movePacket = (PlayerMoveC2SPacket) packet;
                // Nếu packet có thay đổi góc (look) thì chặn lại
                if (movePacket.changesLook()) {
                    ci.cancel();
                }
            }
        }
    }
}
