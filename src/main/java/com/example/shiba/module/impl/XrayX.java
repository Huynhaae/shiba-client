package com.example.shiba.module.impl;

import com.example.shiba.module.Category;
import com.example.shiba.module.Module;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XrayX extends Module {

    public int range = 24;
    public int rescanIntervalTicks = 20;

    public record OreEntry(BlockPos pos, float r, float g, float b) {}

    private static final Map<Block, float[]> ORE_COLORS = new HashMap<>();

    static {
        // Than - den
        ORE_COLORS.put(Blocks.COAL_ORE, new float[]{0.15F, 0.15F, 0.15F});
        ORE_COLORS.put(Blocks.DEEPSLATE_COAL_ORE, new float[]{0.15F, 0.15F, 0.15F});

        // Kim cuong - xanh la (KC)
        ORE_COLORS.put(Blocks.DIAMOND_ORE, new float[]{0.2F, 1.0F, 0.85F});
        ORE_COLORS.put(Blocks.DEEPSLATE_DIAMOND_ORE, new float[]{0.2F, 1.0F, 0.85F});

        // Sat - trang
        ORE_COLORS.put(Blocks.IRON_ORE, new float[]{0.9F, 0.9F, 0.9F});
        ORE_COLORS.put(Blocks.DEEPSLATE_IRON_ORE, new float[]{0.9F, 0.9F, 0.9F});

        // Luu ly - xanh dam
        ORE_COLORS.put(Blocks.LAPIS_ORE, new float[]{0.1F, 0.2F, 0.9F});
        ORE_COLORS.put(Blocks.DEEPSLATE_LAPIS_ORE, new float[]{0.1F, 0.2F, 0.9F});

        // Vang - vang
        ORE_COLORS.put(Blocks.GOLD_ORE, new float[]{1.0F, 0.85F, 0.1F});
        ORE_COLORS.put(Blocks.DEEPSLATE_GOLD_ORE, new float[]{1.0F, 0.85F, 0.1F});
        ORE_COLORS.put(Blocks.NETHER_GOLD_ORE, new float[]{1.0F, 0.85F, 0.1F});

        // Ngoc luc bao - xanh la dam
        ORE_COLORS.put(Blocks.EMERALD_ORE, new float[]{0.1F, 0.8F, 0.3F});
        ORE_COLORS.put(Blocks.DEEPSLATE_EMERALD_ORE, new float[]{0.1F, 0.8F, 0.3F});

        // Redstone - do
        ORE_COLORS.put(Blocks.REDSTONE_ORE, new float[]{0.9F, 0.1F, 0.1F});
        ORE_COLORS.put(Blocks.DEEPSLATE_REDSTONE_ORE, new float[]{0.9F, 0.1F, 0.1F});

        // Ancient debris - tim nau
        ORE_COLORS.put(Blocks.ANCIENT_DEBRIS, new float[]{0.55F, 0.3F, 0.25F});

        // Lava - cam
        ORE_COLORS.put(Blocks.LAVA, new float[]{1.0F, 0.45F, 0.0F});
    }

    private final List<OreEntry> foundOres = new ArrayList<>();
    private int tickCounter = 0;

    public XrayX() {
        super("XrayX", "Hien thi khoang san xuyen tuong, phan loai theo mau.", Category.RENDER);
    }

    @Override
    protected void onEnable() {
        foundOres.clear();
        tickCounter = 0;
    }

    @Override
    protected void onDisable() {
        foundOres.clear();
    }

    @Override
    public void onTick() {
        tickCounter++;
        if (tickCounter < rescanIntervalTicks) return;
        tickCounter = 0;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        foundOres.clear();
        BlockPos center = mc.player.getBlockPos();

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = center.add(x, y, z);
                    Block block = mc.world.getBlockState(pos).getBlock();
                    float[] color = ORE_COLORS.get(block);
                    if (color != null) {
                        foundOres.add(new OreEntry(pos, color[0], color[1], color[2]));
                        if (foundOres.size() > 400) return;
                    }
                }
            }
        }
    }

    public List<OreEntry> getFoundOres() {
        return foundOres;
    }
}
