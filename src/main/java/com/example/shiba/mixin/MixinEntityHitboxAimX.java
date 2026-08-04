package com.example.shiba.mixin;

import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.impl.AimX;
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
public class MixinEntityHitboxAimX {

    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true)
    private void onGetBoundingBox(CallbackInfoReturnable<Box> cir) {
        Entity entity = (Entity) (Object) this;
        // Không áp dụng cho chính người chơi
        if (entity instanceof PlayerEntity && entity == MinecraftClient.getInstance().player) return;
        if (!(entity instanceof LivingEntity)) return;

        AimX aimX = ModuleManager.AIMX;
        if (aimX == null || !aimX.isEnabled()) return;

        // Chỉ mở rộng hitbox cho target hiện tại của AimX
        if (aimX.getTarget() != entity) return;

        float expand = aimX.getHitboxExpand();
        if (expand == 0) return;

        Box box = cir.getReturnValue();
        if (box == null) return;

        // Mở rộng hitbox theo cả 3 chiều (giống hitboxBV)
        Box expanded = box.expand(expand, expand, expand);
        cir.setReturnValue(expanded);
    }
}
