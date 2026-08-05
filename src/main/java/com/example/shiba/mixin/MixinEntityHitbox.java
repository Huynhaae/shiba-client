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

@Mixin(Entity.class)
public class MixinEntityHitbox {

    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true)
    private void onGetBoundingBox(CallbackInfoReturnable<Box> cir) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof PlayerEntity && entity == MinecraftClient.getInstance().player) return;
        if (!(entity instanceof LivingEntity)) return;

        HitboxBV module = ModuleManager.HITBOXBV;
        if (module == null || !module.isEnabled()) return;

        float w = module.getWidth();
        float h = module.getHeight();
        if (w == 0 && h == 0) return;

        Box box = cir.getReturnValue();
        if (box == null) return;

        Box expanded = new Box(
            box.minX - w,
            box.minY,
            box.minZ - w,
            box.maxX + w,
            box.maxY + h,
            box.maxZ + w
        );
        cir.setReturnValue(expanded);
    }
}
