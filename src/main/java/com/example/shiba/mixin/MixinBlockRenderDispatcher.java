package com.example.shiba.mixin;

import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.impl.XRay;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderDispatcher;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockRenderDispatcher.class)
public class MixinBlockRenderDispatcher {

    @Shadow private BlockModels models;
    @Shadow private BlockModelRenderer blockModelRenderer;

    @Inject(method = "renderBlock", at = @At("HEAD"), cancellable = true)
    private void onRenderBlock(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrix, VertexConsumer vertexConsumer, boolean cull, Random random, CallbackInfo ci) {
        XRay xray = ModuleManager.XRAY;
        if (xray != null && xray.isEnabled()) {
            if (!xray.shouldRenderBlock(state.getBlock())) {
                // Bỏ qua render block không cần thiết
                ci.cancel();
            }
        }
    }

    @Inject(method = "getBlockModel", at = @At("HEAD"), cancellable = true)
    private void onGetBlockModel(BlockState state, BlockPos pos, CallbackInfoReturnable<BakedModel> cir) {
        XRay xray = ModuleManager.XRAY;
        if (xray != null && xray.isEnabled()) {
            if (!xray.shouldRenderBlock(state.getBlock())) {
                // Trả về model không render (hoặc model trong suốt)
                // Để đơn giản, không làm gì vì renderBlock đã cancel
            }
        }
    }
}
