package com.example.shiba.module.impl;

import com.example.shiba.module.Category;
import com.example.shiba.module.Module;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class XrayX extends Module {

    public int range = 24;
    public int rescanIntervalTicks = 20;

    private static final Set<Block> TARGET_ORES = new HashSet<>(List.of(
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE,
            Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
            Blocks.ANCIENT_DEBRIS,
            Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE
    ));

    private final List<BlockPos> foundOres = new ArrayList<>();
    private int tickCounter = 0;

    public XrayX() {
        super("XrayX", "Hien thi khoang san xuyen tuong.", Category.RENDER);
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
                    if (TARGET_ORES.contains(block)) {
                        foundOres.add(pos);
                        if (foundOres.size() > 300) return;
                    }
                }
            }
        }
    }

    public List<BlockPos> getFoundOres() {
        return foundOres;
    }
}
