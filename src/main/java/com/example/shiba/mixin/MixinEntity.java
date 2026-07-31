package com.example.shiba.mixin;

import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.impl.Hitbox;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinEntity {

    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true)
    private void shiba$expandBoundingBox(CallbackInfoReturnable<Box> cir) {
        Entity self = (Entity) (Object) this;

        if (!(self instanceof LivingEntity)) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || self == mc.player) return;

        Hitbox hitbox = ModuleManager.HITBOX;
        if (hitbox == null || !hitbox.isEnabled()) return;

        cir.setReturnValue(cir.getReturnValue().expand(hitbox.expand));
    }
}
