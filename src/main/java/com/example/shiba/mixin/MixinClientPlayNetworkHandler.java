package com.example.shiba.mixin;

import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.impl.AuraX;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        AuraX aura = ModuleManager.AURAX;
        if (aura == null || !aura.isEnabled()) return;

        // Nếu mode Silent và packet là rotation thì chặn
        if (aura.getMode().equals("Silent")) {
            if (packet instanceof PlayerMoveC2SPacket) {
                PlayerMoveC2SPacket movePacket = (PlayerMoveC2SPacket) packet;
                // Nếu packet có thay đổi yaw/pitch thì chặn
                if (movePacket.changesLook()) {
                    ci.cancel();
                }
            }
        }
    }
}
