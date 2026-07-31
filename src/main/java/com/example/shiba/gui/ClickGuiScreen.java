package com.example.shiba.gui;

import com.example.shiba.ShibaClient;
import com.example.shiba.config.ConfigManager;
import com.example.shiba.module.Module;
import com.example.shiba.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {

    private static final int PANEL_WIDTH = 210;
    private static final int ROW_HEIGHT = 22;
    private static final int SPACING = 4;
    private static final int PADDING = 8;
    private static final int HEADER_HEIGHT = 28;

    private static final int COLOR_PANEL_BG = 0xE6161618;
    private static final int COLOR_PANEL_BORDER = 0xFF2A2A30;
    private static final int COLOR_HEADER = 0xFF7C5CFF;

    private int panelX;
    private int panelY;
    private int totalHeight;

    private final List<Row> rows = new ArrayList<>();

    private record Row(Module module, int x, int y, int w, int h) {}

    public ClickGuiScreen() {
        super(Text.literal(ShibaClient.MOD_NAME));
    }

    @Override
    protected void init() {
        panelX = this.width / 2 - PANEL_WIDTH / 2;
        panelY = 40;
        rows.clear();

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
            rows.add(new Row(module, rowX, finalY, rowW, ROW_HEIGHT));

            y += ROW_HEIGHT + SPACING;
        }

        totalHeight = y - panelY;
    }

    private Text labelFor(Module module) {
        String state = module.isEnabled() ? "ON" : "OFF";
        String bind = module.keyCode != -1
                ? " [" + InputUtil.fromKeyCode(module.keyCode, 0).getLocalizedText().getString() + "]"
                : "";
        return Text.literal(module.getName() + bind + "  ·  " + state);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(panelX - 1, panelY - 1, panelX + PANEL_WIDTH + 1, panelY + totalHeight + 1, COLOR_PANEL_BORDER);
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + totalHeight, COLOR_PANEL_BG);
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + HEADER_HEIGHT, COLOR_HEADER);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title,
                panelX + PANEL_WIDTH / 2, panelY + 10, 0xFFFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            for (Row row : rows) {
                if (mouseX >= row.x() && mouseX <= row.x() + row.w()
                        && mouseY >= row.y() && mouseY <= row.y() + row.h()) {
                    if (this.client != null) {
                        this.client.setScreen(new ModuleSettingsScreen(row.module(), this));
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
}
