package com.example.shiba.mixin;

import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.impl.XRay;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    @Inject(method = "isBlockOccluded", at = @At("HEAD"), cancellable = true)
    private void onIsBlockOccluded(BlockState state, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        XRay xray = ModuleManager.XRAY;
        if (xray != null && xray.isEnabled()) {
            // Nếu block không nên hiển thị, coi nó là bị che khuất (không render)
            if (!xray.shouldRenderBlock(state.getBlock())) {
                cir.setReturnValue(true);
            }
        }
    }
}
