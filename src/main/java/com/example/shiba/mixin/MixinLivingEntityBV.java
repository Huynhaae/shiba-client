package com.example.shiba.mixin;

import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.impl.AimX;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {

    @Inject(method = "setYaw", at = @At("HEAD"), cancellable = true)
    private void onSetYaw(float yaw, CallbackInfo ci) {
        AimX aimX = ModuleManager.AIMX;
        if (aimX != null && aimX.isEnabled() && aimX.getMode().equals("Silent")) {
            // Chặn setYaw khi Silent mode đang bật
            ci.cancel();
        }
    }

    @Inject(method = "setPitch", at = @At("HEAD"), cancellable = true)
    private void onSetPitch(float pitch, CallbackInfo ci) {
        AimX aimX = ModuleManager.AIMX;
        if (aimX != null && aimX.isEnabled() && aimX.getMode().equals("Silent")) {
            // Chặn setPitch khi Silent mode đang bật
            ci.cancel();
        }
    }
}
