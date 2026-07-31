package com.example.shiba.gui;

import com.example.shiba.config.ConfigManager;
import com.example.shiba.module.Module;
import com.example.shiba.module.impl.Hitbox;
import com.example.shiba.module.impl.Reach;
import com.example.shiba.module.impl.TriggerBot;
import com.example.shiba.module.impl.Aura;
import com.example.shiba.module.impl.ESP;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ModuleSettingsScreen extends Screen {

    private static final int PANEL_WIDTH = 220;
    private static final int ROW_HEIGHT = 20;
    private static final int SLIDER_HEIGHT = 18;
    private static final int SPACING = 4;
    private static final int PADDING = 8;
    private static final int HEADER_HEIGHT = 26;

    private static final int COLOR_PANEL_BG = 0xE6161618;
    private static final int COLOR_PANEL_BORDER = 0xFF2A2A30;
    private static final int COLOR_HEADER = 0xFF7C5CFF;

    private final Module module;
    private final Screen parent;

    private int panelX;
    private int panelY;
    private int totalHeight;

    private boolean awaitingKeybind = false;
    private ButtonWidget keybindButton;

    public ModuleSettingsScreen(Module module, Screen parent) {
        super(Text.literal(module.getName() + " Settings"));
        this.module = module;
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelX = this.width / 2 - PANEL_WIDTH / 2;
        panelY = 30;

        int y = panelY + HEADER_HEIGHT + PADDING;
        int rowX = panelX + PADDING;
        int rowW = PANEL_WIDTH - PADDING * 2;

        if (module instanceof Hitbox hitbox) {
            y = addSlider(y, rowX, rowW, hitbox.expand, 1.0,
                    v -> hitbox.expand = v, v -> "Expand: " + String.format("%.2f", v));

            ButtonWidget renderToggle = ButtonWidget.builder(
                    Text.literal("Render Outline: " + (hitbox.renderOutline ? "ON" : "OFF")),
                    btn -> {
                        hitbox.renderOutline = !hitbox.renderOutline;
                        btn.setMessage(Text.literal("Render Outline: " + (hitbox.renderOutline ? "ON" : "OFF")));
                        ConfigManager.save();
                    }
            ).dimensions(rowX, y, rowW, ROW_HEIGHT).build();
            this.addDrawableChild(renderToggle);
            y += ROW_HEIGHT + SPACING;
        }

        if (module instanceof Reach reach) {
            y = addSlider(y, rowX, rowW, reach.reach, 20.0,
                    v -> reach.reach = v, v -> "Reach: " + String.format("%.2f", v));
        }

        if (module instanceof TriggerBot tb) {
            y = addSlider(y, rowX, rowW, tb.fov, 60.0,
                    v -> tb.fov = v, v -> "FOV: " + String.format("%.1f", v));
            y = addSlider(y, rowX, rowW, tb.range, 8.0,
                    v -> tb.range = v, v -> "Range: " + String.format("%.2f", v));
            y = addSlider(y, rowX, rowW, tb.attackDelayTicks, 20.0,
                    v -> tb.attackDelayTicks = v, v -> "Delay: " + String.format("%.0f ticks", v));

            ButtonWidget critToggle = ButtonWidget.builder(
                    Text.literal("Crit: " + (tb.critEnabled ? "ON" : "OFF")),
                    btn -> {
                        tb.critEnabled = !tb.critEnabled;
                        btn.setMessage(Text.literal("Crit: " + (tb.critEnabled ? "ON" : "OFF")));
                        ConfigManager.save();
                    }
            ).dimensions(rowX, y, rowW, ROW_HEIGHT).build();
            this.addDrawableChild(critToggle);
            y += ROW_HEIGHT + SPACING;
        }

        if (module instanceof Aura aura) {
            y = addSlider(y, rowX, rowW, aura.range, 8.0,
                    v -> aura.range = v, v -> "Range: " + String.format("%.2f", v));
            y = addSlider(y, rowX, rowW, aura.rotationSpeed, 360.0,
                    v -> aura.rotationSpeed = v, v -> "Rotate Speed: " + String.format("%.0f", v));
            y = addSlider(y, rowX, rowW, aura.attackDelayTicks, 20.0,
                    v -> aura.attackDelayTicks = v, v -> "Delay: " + String.format("%.0f ticks", v));
        }

        if (module instanceof ESP esp) {
            y = addSlider(y, rowX, rowW, esp.range, 5000.0,
                    v -> esp.range = v, v -> "Range: " + String.format("%.0f", v));
        }

        keybindButton = ButtonWidget.builder(
                keybindLabel(),
                btn -> {
                    awaitingKeybind = true;
                    btn.setMessage(Text.literal("Press a key..."));
                }
        ).dimensions(rowX, y, rowW, ROW_HEIGHT).build();
        this.addDrawableChild(keybindButton);
        y += ROW_HEIGHT + SPACING;

        ButtonWidget backButton = ButtonWidget.builder(
                Text.literal("Back"),
                btn -> this.close()
        ).dimensions(rowX, y, rowW, ROW_HEIGHT).build();
        this.addDrawableChild(backButton);
        y += ROW_HEIGHT + SPACING;

        totalHeight = y - panelY;
    }

    private Text keybindLabel() {
        if (module.keyCode == -1) return Text.literal("Keybind: None");
        String keyName = InputUtil.fromKeyCode(module.keyCode, 0).getLocalizedText().getString();
        return Text.literal("Keybind: " + keyName);
    }

    private int addSlider(int y, int rowX, int rowW, double current, double max,
                           java.util.function.DoubleConsumer setter,
                           java.util.function.DoubleFunction<String> label) {
        SliderWidget slider = new SliderWidget(rowX, y, rowW, SLIDER_HEIGHT, Text.literal(""), current / max) {
            {
                setMessage(Text.literal(label.apply(current)));
            }
            @Override
            protected void updateMessage() {
                this.setMessage(Text.literal(label.apply(this.value * max)));
            }
            @Override
            protected void applyValue() {
                setter.accept(this.value * max);
                ConfigManager.save();
            }
        };
        this.addDrawableChild(slider);
        return y + SLIDER_HEIGHT + SPACING;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(panelX - 1, panelY - 1, panelX + PANEL_WIDTH + 1, panelY + totalHeight + 1, COLOR_PANEL_BORDER);
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + totalHeight, COLOR_PANEL_BG);
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + HEADER_HEIGHT, COLOR_HEADER);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title,
                panelX + PANEL_WIDTH / 2, panelY + 8, 0xFFFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (awaitingKeybind) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                module.keyCode = -1;
            } else {
                module.keyCode = keyCode;
            }
            awaitingKeybind = false;
            keybindButton.setMessage(keybindLabel());
            ConfigManager.save();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
