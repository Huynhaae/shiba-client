package com.example.shiba.mixin;

import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.impl.Reach;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity {

    @Inject(method = "getEntityInteractionRange", at = @At("RETURN"), cancellable = true)
    private void shiba$expandReach(CallbackInfoReturnable<Double> cir) {
        PlayerEntity self = (PlayerEntity) (Object) this;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || self != mc.player) return;

        Reach reach = ModuleManager.REACH;
        if (reach == null || !reach.isEnabled()) return;

        cir.setReturnValue(reach.reach);
    }
}
