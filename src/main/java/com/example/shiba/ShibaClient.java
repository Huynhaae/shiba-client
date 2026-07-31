package com.example.shiba;

import com.example.shiba.config.ConfigManager;
import com.example.shiba.gui.ClickGuiScreen;
import com.example.shiba.module.Module;
import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.impl.Hitbox;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
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

            if (client.currentScreen == null) {
                long handle = client.getWindow().getHandle();
                for (Module m : ModuleManager.getModules()) {
                    if (m.keyCode != -1) {
                        boolean down = InputUtil.isKeyPressed(handle, m.keyCode);
                        m.tickKeybind(down);
                    }
                }
            }
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
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.world == null || mc.player == null) return;

            var camPos = context.camera().getPos();

            Hitbox hitbox = ModuleManager.HITBOX;
            if (hitbox.isEnabled() && hitbox.renderOutline) {
                for (Entity entity : mc.world.getEntities()) {
                    if (entity == mc.player) continue;
                    if (entity.squaredDistanceTo(mc.player) > 64 * 64) continue;
                    hitbox.renderExpandedBox(context.matrixStack(), context.consumers(), entity, camPos);
                }
            }

            var esp = ModuleManager.ESP;
            if (esp.isEnabled()) {
                RenderSystem.disableDepthTest();
                var buffer = context.consumers().getBuffer(RenderLayer.getLines());
                for (Entity entity : mc.world.getEntities()) {
                    if (entity == mc.player) continue;
                    if (!(entity instanceof net.minecraft.entity.LivingEntity)) continue;
                    if (entity.squaredDistanceTo(mc.player) > esp.range * esp.range) continue;

                    Box box = entity.getBoundingBox().offset(-camPos.x, -camPos.y, -camPos.z);
                    context.matrixStack().push();
                    WorldRenderer.drawBox(
                            context.matrixStack(), buffer,
                            box.minX, box.minY, box.minZ,
                            box.maxX, box.maxY, box.maxZ,
                            0.2F, 1.0F, 0.3F, 0.9F
                    );
                    context.matrixStack().pop();
                }
                RenderSystem.enableDepthTest();
            }
        });
    }
}
