package com.example.shiba.mixin;

import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.impl.HitboxBV;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinLivingEntityBV {

    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true)
    private void onGetBoundingBox(CallbackInfoReturnable<Box> cir) {
        HitboxBV module = ModuleManager.getModule(HitboxBV.class);
        if (module == null || !module.isEnabled()) return;

        Entity entity = (Entity) (Object) this;
        if (entity instanceof PlayerEntity player &&
                player == MinecraftClient.getInstance().player) {
            return;
        }

        Box original = cir.getReturnValue();
        if (original == null) return;

        double w = module.getWidth();
        double h = module.getHeight();
        Box expanded = original.expand(w, h, w);
        cir.setReturnValue(expanded);
    }
}
