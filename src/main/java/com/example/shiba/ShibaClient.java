package com.example.shiba;

import com.example.shiba.config.ConfigManager;
import com.example.shiba.gui.ClickGuiScreen;
import com.example.shiba.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public class ShibaClient implements ClientModInitializer {
    public static final String MOD_ID = "shiba";
    public static final String MOD_NAME = "Shiba";

    public static KeyBinding openMenuKey;

    @Override
    public void onInitializeClient() {
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.shiba.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.shiba"
        ));

        ConfigManager.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                client.setScreen(new ClickGuiScreen());
            }
            ModuleManager.tick();
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.options.hudHidden) return;

            int y = 4;
            if (ModuleManager.COORDS.isEnabled()) {
                BlockPos pos = mc.player.getBlockPos();
                String text = "XYZ: " + pos.getX() + " " + pos.getY() + " " + pos.getZ();
                context.drawTextWithShadow(mc.textRenderer, text, 4, y, 0xFFFFFF);
                y += 10;
            }
            if (ModuleManager.FPS.isEnabled()) {
                context.drawTextWithShadow(mc.textRenderer,
                        mc.getCurrentFps() + " FPS", 4, y, 0xFFFFFF);
            }
            ModuleManager.CRIT.render(context);
        });
    }
}
