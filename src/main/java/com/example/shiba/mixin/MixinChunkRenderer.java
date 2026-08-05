package com.example.shiba.mixin;

import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.impl.XRay;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkRenderer.class)
public class MixinChunkRenderer {

    @Inject(method = "shouldRenderBlock", at = @At("HEAD"), cancellable = true)
    private void onShouldRenderBlock(BlockState state, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        XRay xray = ModuleManager.XRAY;
        if (xray != null && xray.isEnabled()) {
            if (!xray.shouldRenderBlock(state.getBlock())) {
                cir.setReturnValue(false);
            }
        }
    }
}
