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
        if (aura != null && aura.isEnabled() && aura.getMode().equals("Silent")) {
            if (packet instanceof PlayerMoveC2SPacket) {
                PlayerMoveC2SPacket movePacket = (PlayerMoveC2SPacket) packet;
                // Chỉ chặn nếu packet có thay đổi góc (look), không chặn di chuyển
                if (movePacket.changesLook()) {
                    ci.cancel();
                }
            }
        }
    }
}
