package com.example.shiba.mixin;

import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.impl.WTap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {

    @Inject(method = "attackEntity", at = @At("HEAD"))
    private void shiba$onAttack(net.minecraft.entity.player.PlayerEntity player, Entity target, CallbackInfo ci) {
        WTap wtap = ModuleManager.WTAP;
        if (wtap == null || !wtap.isEnabled()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || player != mc.player) return;

        if (mc.player.isSprinting()) {
            mc.player.setSprinting(false);
            wtap.markTap();
        }
    }
}
