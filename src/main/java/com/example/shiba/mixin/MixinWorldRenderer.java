package com.example.shiba.mixin;

import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.impl.XRay;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    @ModifyVariable(method = "render", at = @At("STORE"), ordinal = 0)
    private BlockState modifyBlockState(BlockState state) {
        XRay xray = ModuleManager.XRAY;
        if (xray != null && xray.isEnabled()) {
            if (!xray.shouldRenderBlock(state.getBlock())) {
                return Blocks.AIR.getDefaultState();
            }
        }
        return state;
    }
}
