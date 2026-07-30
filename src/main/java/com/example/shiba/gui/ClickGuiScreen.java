package com.example.shiba.gui;

import com.example.shiba.ShibaClient;
import com.example.shiba.config.ConfigManager;
import com.example.shiba.module.Module;
import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.impl.Hitbox;
import com.example.shiba.module.impl.Reach;
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
                ValueSlider expandSlider = new ValueSlider(
                        rowX, y, rowW, SLIDER_HEIGHT,
                        hitbox.expand, 1.0,
                        v -> hitbox.expand = v,
                        v -> "Expand: " + String.format("%.2f", v)
                );
                this.addDrawableChild(expandSlider);
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

            if (module instanceof Reach reach) {
                ValueSlider reachSlider = new ValueSlider(
                        rowX, y, rowW, SLIDER_HEIGHT,
                        reach.reach, 6.0,
                        v -> reach.reach = v,
                        v -> "Reach: " + String.format("%.2f", v)
                );
                this.addDrawableChild(reachSlider);
                y += SLIDER_HEIGHT + SPACING;
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
            if (module instanceof Reach) {
                totalHeight += SLIDER_HEIGHT + SPACING;
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

    private static class ValueSlider extends SliderWidget {
        private final double max;
        private final java.util.function.DoubleConsumer setter;
        private final java.util.function.DoubleFunction<String> label;

        public ValueSlider(int x, int y, int width, int height,
                            double current, double max,
                            java.util.function.DoubleConsumer setter,
                            java.util.function.DoubleFunction<String> label) {
            super(x, y, width, height, Text.literal(""), current / max);
            this.max = max;
            this.setter = setter;
            this.label = label;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Text.literal(label.apply(this.value * max)));
        }

        @Override
        protected void applyValue() {
            setter.accept(this.value * max);
        }
    }
}
