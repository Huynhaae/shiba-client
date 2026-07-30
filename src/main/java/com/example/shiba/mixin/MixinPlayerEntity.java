package com.example.shiba.mixin;

import com.example.shiba.ShibaClient;
import com.example.shiba.module.impl.Hitbox;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity {
    @Inject(method = "getEntityInteractionRange", at = @At("RETURN"), cancellable = true)
    private void onGetEntityInteractionRange(CallbackInfoReturnable<Double> cir) {
        Hitbox hitbox = (Hitbox) ShibaClient.moduleManager.getModuleByName("Hitbox");
        if (hitbox != null && hitbox.isToggled()) {
            double defaultReach = cir.getReturnValue();
            cir.setReturnValue(defaultReach + Hitbox.expand);
        }
    }
}
