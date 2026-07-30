package com.example.shiba.gui;

import com.example.shiba.ShibaClient;
import com.example.shiba.config.ConfigManager;
import com.example.shiba.module.Module;
import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.impl.Hitbox;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ClickGuiScreen extends Screen {

    private static final int PANEL_WIDTH = 210;
    private static final int ROW_HEIGHT = 22;
    private static final int SLIDER_HEIGHT = 18;
    private static final int SPACING = 4;
    private static final int PADDING = 8;
    private static final int HEADER_HEIGHT = 28;

    private static final int COLOR_PANEL_BG = 0xE6161618;
    private static final int COLOR_PANEL_BORDER = 0xFF2A2A30;
    private static final int COLOR_HEADER = 0xFF7C5CFF;
    private static final int COLOR_ROW_OFF = 0x99222226;
    private static final int COLOR_ROW_ON = 0xCC7C5CFF;
    private static final int COLOR_TEXT = 0xFFEDEDF2;
    private static final int COLOR_TEXT_DIM = 0xFFA0A0AC;

    private int panelX;
    private int panelY;

    public ClickGuiScreen() {
        super(Text.literal(ShibaClient.MOD_NAME));
    }

    @Override
    protected void init() {
        panelX = this.width / 2 - PANEL_WIDTH / 2;
        panelY = 40;

        int y = panelY + HEADER_HEIGHT + PADDING;

        for (Module module : ModuleManager.getModules()) {
            int rowX = panelX + PADDING;
            int rowW = PANEL_WIDTH - PADDING * 2;
            final int finalY = y;

            ButtonWidget button = ButtonWidget.builder(labelFor(module), btn -> {
                module.toggle();
                btn.setMessage(labelFor(module));
                ConfigManager.save();
            }).dimensions(rowX, finalY, rowW, ROW_HEIGHT).build();

            this.addDrawableChild(button);
            y += ROW_HEIGHT + SPACING;

            if (module instanceof Hitbox hitbox) {
                HitboxExpandSlider slider = new HitboxExpandSlider(rowX, y, rowW, SLIDER_HEIGHT, hitbox);
                this.addDrawableChild(slider);
                y += SLIDER_HEIGHT + SPACING;

                ButtonWidget renderToggle = ButtonWidget.builder(
                        renderLabelFor(hitbox),
                        btn -> {
                            hitbox.renderOutline = !hitbox.renderOutline;
                            btn.setMessage(renderLabelFor(hitbox));
                            ConfigManager.save();
                        }
                ).dimensions(rowX, y, rowW, ROW_HEIGHT - 2).build();

                this.addDrawableChild(renderToggle);
                y += (ROW_HEIGHT - 2) + SPACING;
            }
        }
    }

    private Text labelFor(Module module) {
        String state = module.isEnabled() ? "ON" : "OFF";
        return Text.literal(module.getName() + "  ·  " + state);
    }

    private Text renderLabelFor(Hitbox hitbox) {
        return Text.literal("Render Outline: " + (hitbox.renderOutline ? "ON" : "OFF"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int totalHeight = HEADER_HEIGHT + PADDING;
        for (Module module : ModuleManager.getModules()) {
            totalHeight += ROW_HEIGHT + SPACING;
            if (module instanceof Hitbox) {
                totalHeight += SLIDER_HEIGHT + SPACING;
                totalHeight += (ROW_HEIGHT - 2) + SPACING;
            }
        }

        context.fill(panelX - 1, panelY - 1, panelX + PANEL_WIDTH + 1, panelY + totalHeight + 1, COLOR_PANEL_BORDER);
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + totalHeight, COLOR_PANEL_BG);
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + HEADER_HEIGHT, COLOR_HEADER);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title,
                panelX + PANEL_WIDTH / 2, panelY + 10, 0xFFFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT || keyCode == GLFW.GLFW_KEY_G) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static class HitboxExpandSlider extends SliderWidget {
        private final Hitbox hitbox;
        private static final double MAX_EXPAND = 1.0;

        public HitboxExpandSlider(int x, int y, int width, int height, Hitbox hitbox) {
            super(x, y, width, height, Text.literal(""), hitbox.expand / MAX_EXPAND);
            this.hitbox = hitbox;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Text.literal(String.format("Expand: %.2f", hitbox.expand)));
        }

        @Override
        protected void applyValue() {
            hitbox.expand = this.value * MAX_EXPAND;
        }
    }
}
