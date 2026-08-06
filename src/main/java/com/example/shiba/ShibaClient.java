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
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
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
                    if (m.getKeybind() != -1) {
    boolean down = InputUtil.isKeyPressed(handle, m.getKeybind());
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
                renderEspThroughWalls(mc, camPos, esp.range);
            }

            var xray = ModuleManager.XRAYX;
            if (xray.isEnabled()) {
                renderXrayOres(mc, camPos, xray);
            }
        });
    }

   private void renderXrayOres(MinecraftClient mc, Vec3d camPos, com.example.shiba.module.impl.XrayX xray) {
        var ores = xray.getFoundOres();
        if (ores.isEmpty()) return;

        Matrix4f matrix = new Matrix4f();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.lineWidth(2.0F);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        for (var ore : ores) {
            Box box = new Box(ore.pos()).offset(-camPos.x, -camPos.y, -camPos.z);
            addBoxLines(buffer, matrix, box, ore.r(), ore.g(), ore.b(), 0.9F);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private void renderEspThroughWalls(MinecraftClient mc, Vec3d camPos, double range) {
        Matrix4f matrix = new Matrix4f();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.lineWidth(2.0F);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        boolean any = false;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof net.minecraft.entity.LivingEntity)) continue;
            if (entity.squaredDistanceTo(mc.player) > range * range) continue;

            Box box = entity.getBoundingBox().offset(-camPos.x, -camPos.y, -camPos.z);
            addBoxLines(buffer, matrix, box, 0.2F, 1.0F, 0.3F, 0.9F);
            any = true;
        }

        if (any) {
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private void addBoxLines(BufferBuilder buffer, Matrix4f matrix, Box box, float r, float g, float b, float a) {
        float minX = (float) box.minX, minY = (float) box.minY, minZ = (float) box.minZ;
        float maxX = (float) box.maxX, maxY = (float) box.maxY, maxZ = (float) box.maxZ;

        float[][] corners = {
                {minX, minY, minZ}, {maxX, minY, minZ},
                {maxX, minY, maxZ}, {minX, minY, maxZ},
                {minX, maxY, minZ}, {maxX, maxY, minZ},
                {maxX, maxY, maxZ}, {minX, maxY, maxZ}
        };

        int[][] edges = {
                {0,1},{1,2},{2,3},{3,0},
                {4,5},{5,6},{6,7},{7,4},
                {0,4},{1,5},{2,6},{3,7}
        };

        for (int[] edge : edges) {
            float[] p1 = corners[edge[0]];
            float[] p2 = corners[edge[1]];
            buffer.vertex(matrix, p1[0], p1[1], p1[2]).color(r, g, b, a);
            buffer.vertex(matrix, p2[0], p2[1], p2[2]).color(r, g, b, a);
        }
    }
}
