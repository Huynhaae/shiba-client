package com.example.shiba.module.impl;

import com.example.shiba.module.Module;
import com.example.shiba.module.Category;
import com.example.shiba.module.settings.BooleanSetting;
import com.example.shiba.module.settings.ModeSetting;
import com.example.shiba.module.settings.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.util.HashSet;
import java.util.Set;

public class XRay extends Module {
    public final ModeSetting mode = new ModeSetting("Mode", "Ores", "Ores", "All", "Custom");
    public final NumberSetting opacity = new NumberSetting("Opacity", 0.0, 1.0, 0.5, 0.05);
    public final BooleanSetting showChests = new BooleanSetting("ShowChests", true);
    public final BooleanSetting showSpawners = new BooleanSetting("ShowSpawners", true);
    public final BooleanSetting showOres = new BooleanSetting("ShowOres", true);
    public final BooleanSetting showStone = new BooleanSetting("ShowStone", false);
    public final BooleanSetting showDirt = new BooleanSetting("ShowDirt", false);
    public final BooleanSetting showWood = new BooleanSetting("ShowWood", false);

    private Set<Block> visibleBlocks = new HashSet<>();

    public XRay() {
        super("XRay", "Nhìn xuyên block (client-side)", Category.RENDER);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        updateVisibleBlocks();
        if (mc.world != null) {
            mc.worldRenderer.reload();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        visibleBlocks.clear();
        if (mc.world != null) {
            mc.worldRenderer.reload();
        }
    }

    @Override
    public void onTick() {
        if (isEnabled()) {
            updateVisibleBlocks();
        }
    }

    public boolean shouldRenderBlock(Block block) {
        if (!isEnabled()) return true;
        return visibleBlocks.contains(block);
    }

    private void updateVisibleBlocks() {
        visibleBlocks.clear();
        if (!isEnabled()) return;

        String modeValue = mode.getValue();

        if (modeValue.equals("Ores") || modeValue.equals("All") || showOres.getValue()) {
            addOres();
        }

        if (showChests.getValue()) {
            visibleBlocks.add(Blocks.CHEST);
            visibleBlocks.add(Blocks.TRAPPED_CHEST);
            visibleBlocks.add(Blocks.BARREL);
            visibleBlocks.add(Blocks.SHULKER_BOX);
            visibleBlocks.add(Blocks.WHITE_SHULKER_BOX);
            visibleBlocks.add(Blocks.ORANGE_SHULKER_BOX);
            visibleBlocks.add(Blocks.MAGENTA_SHULKER_BOX);
            visibleBlocks.add(Blocks.LIGHT_BLUE_SHULKER_BOX);
            visibleBlocks.add(Blocks.YELLOW_SHULKER_BOX);
            visibleBlocks.add(Blocks.LIME_SHULKER_BOX);
            visibleBlocks.add(Blocks.PINK_SHULKER_BOX);
            visibleBlocks.add(Blocks.GRAY_SHULKER_BOX);
            visibleBlocks.add(Blocks.LIGHT_GRAY_SHULKER_BOX);
            visibleBlocks.add(Blocks.CYAN_SHULKER_BOX);
            visibleBlocks.add(Blocks.PURPLE_SHULKER_BOX);
            visibleBlocks.add(Blocks.BLUE_SHULKER_BOX);
            visibleBlocks.add(Blocks.BROWN_SHULKER_BOX);
            visibleBlocks.add(Blocks.GREEN_SHULKER_BOX);
            visibleBlocks.add(Blocks.RED_SHULKER_BOX);
            visibleBlocks.add(Blocks.BLACK_SHULKER_BOX);
            visibleBlocks.add(Blocks.ENDER_CHEST);
        }

        if (showSpawners.getValue()) {
            visibleBlocks.add(Blocks.SPAWNER);
            visibleBlocks.add(Blocks.TRIAL_SPAWNER);
            visibleBlocks.add(Blocks.VAULT);
        }

        if (showStone.getValue()) {
            visibleBlocks.add(Blocks.STONE);
            visibleBlocks.add(Blocks.COBBLESTONE);
            visibleBlocks.add(Blocks.GRANITE);
            visibleBlocks.add(Blocks.DIORITE);
            visibleBlocks.add(Blocks.ANDESITE);
            visibleBlocks.add(Blocks.DEEPSLATE);
            visibleBlocks.add(Blocks.TUFF);
            visibleBlocks.add(Blocks.CALCITE);
            visibleBlocks.add(Blocks.DRIPSTONE_BLOCK);
        }

        if (showDirt.getValue()) {
            visibleBlocks.add(Blocks.DIRT);
            visibleBlocks.add(Blocks.GRASS_BLOCK);
            visibleBlocks.add(Blocks.COARSE_DIRT);
            visibleBlocks.add(Blocks.ROOTED_DIRT);
            visibleBlocks.add(Blocks.MUD);
            visibleBlocks.add(Blocks.SAND);
            visibleBlocks.add(Blocks.RED_SAND);
            visibleBlocks.add(Blocks.GRAVEL);
        }

        if (showWood.getValue()) {
            visibleBlocks.add(Blocks.OAK_LOG);
            visibleBlocks.add(Blocks.SPRUCE_LOG);
            visibleBlocks.add(Blocks.BIRCH_LOG);
            visibleBlocks.add(Blocks.JUNGLE_LOG);
            visibleBlocks.add(Blocks.ACACIA_LOG);
            visibleBlocks.add(Blocks.DARK_OAK_LOG);
            visibleBlocks.add(Blocks.MANGROVE_LOG);
            visibleBlocks.add(Blocks.CHERRY_LOG);
            visibleBlocks.add(Blocks.OAK_WOOD);
            visibleBlocks.add(Blocks.SPRUCE_WOOD);
            visibleBlocks.add(Blocks.BIRCH_WOOD);
            visibleBlocks.add(Blocks.JUNGLE_WOOD);
            visibleBlocks.add(Blocks.ACACIA_WOOD);
            visibleBlocks.add(Blocks.DARK_OAK_WOOD);
            visibleBlocks.add(Blocks.MANGROVE_WOOD);
            visibleBlocks.add(Blocks.CHERRY_WOOD);
        }
    }

    private void addOres() {
        visibleBlocks.add(Blocks.IRON_ORE);
        visibleBlocks.add(Blocks.DEEPSLATE_IRON_ORE);
        visibleBlocks.add(Blocks.GOLD_ORE);
        visibleBlocks.add(Blocks.DEEPSLATE_GOLD_ORE);
        visibleBlocks.add(Blocks.DIAMOND_ORE);
        visibleBlocks.add(Blocks.DEEPSLATE_DIAMOND_ORE);
        visibleBlocks.add(Blocks.EMERALD_ORE);
        visibleBlocks.add(Blocks.DEEPSLATE_EMERALD_ORE);
        visibleBlocks.add(Blocks.LAPIS_ORE);
        visibleBlocks.add(Blocks.DEEPSLATE_LAPIS_ORE);
        visibleBlocks.add(Blocks.REDSTONE_ORE);
        visibleBlocks.add(Blocks.DEEPSLATE_REDSTONE_ORE);
        visibleBlocks.add(Blocks.COAL_ORE);
        visibleBlocks.add(Blocks.DEEPSLATE_COAL_ORE);
        visibleBlocks.add(Blocks.COPPER_ORE);
        visibleBlocks.add(Blocks.DEEPSLATE_COPPER_ORE);
        visibleBlocks.add(Blocks.NETHER_GOLD_ORE);
        visibleBlocks.add(Blocks.NETHER_QUARTZ_ORE);
        visibleBlocks.add(Blocks.ANCIENT_DEBRIS);
        visibleBlocks.add(Blocks.RAW_IRON_BLOCK);
        visibleBlocks.add(Blocks.RAW_GOLD_BLOCK);
        visibleBlocks.add(Blocks.RAW_COPPER_BLOCK);
        visibleBlocks.add(Blocks.BUDDING_AMETHYST);
    }

    public float getOpacity() {
        return (float) opacity.getValue();
    }
}
