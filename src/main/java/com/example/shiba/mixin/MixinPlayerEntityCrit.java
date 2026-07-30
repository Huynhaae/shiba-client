package com.example.shiba.mixin;

import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.impl.CritDisplay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntityCrit {

    @Inject(method = "attack", at = @At("HEAD"))
    private void shiba$onAttack(Entity target, CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || self != mc.player) return;
        if (!(target instanceof LivingEntity)) return;

        CritDisplay critDisplay = ModuleManager.CRIT;
        if (critDisplay == null || !critDisplay.isEnabled()) return;

        boolean isCritical = !self.isOnGround()
                && !self.isClimbing()
                && !self.isTouchingWater()
                && !self.hasStatusEffect(StatusEffects.BLINDNESS)
                && !self.hasVehicle()
                && !self.isSprinting()
                && self.getVelocity().y < 0.0;

        if (isCritical) {
            critDisplay.trigger(target);
        }
    }
}
