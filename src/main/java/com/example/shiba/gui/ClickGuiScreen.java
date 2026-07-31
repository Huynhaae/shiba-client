package com.example.shiba.gui;

import com.example.shiba.ShibaClient;
import com.example.shiba.config.ConfigManager;
import com.example.shiba.module.Category;
import com.example.shiba.module.Module;
import com.example.shiba.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {

    private static final int PANEL_WIDTH = 220;
    private static final int ROW_HEIGHT = 22;
    private static final int TAB_HEIGHT = 22;
    private static final int SPACING = 4;
    private static final int PADDING = 8;
    private static final int HEADER_HEIGHT = 26;

    private static final int COLOR_PANEL_BG = 0xE6161618;
    private static final int COLOR_PANEL_BORDER = 0xFF2A2A30;
    private static final int COLOR_HEADER = 0xFF7C5CFF;
    private static final int COLOR_TAB_OFF = 0x99222226;
    private static final int COLOR_TAB_ON = 0xCC7C5CFF;
    private static final int COLOR_ROW = 0x99222226;

    private int panelX = 100;
    private int panelY = 40;

    private boolean dragging = false;
    private double dragOffsetX, dragOffsetY;

    private Category selected = Category.COMBAT;

    private final List<Rect> tabRects = new ArrayList<>();
    private final List<Rect> rowRects = new ArrayList<>();

    private record Rect(int x, int y, int w, int h, Object data) {}

    public ClickGuiScreen() {
        super(Text.literal(ShibaClient.MOD_NAME));
    }

    @Override
    protected void init() {
    }

    private void rebuild() {
        tabRects.clear();
        rowRects.clear();

        int tabX = panelX + PADDING;
        int tabW = (PANEL_WIDTH - PADDING * 2 - SPACING * 3) / 4;
        int tabY = panelY + HEADER_HEIGHT + PADDING;

        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            int x = tabX + i * (tabW + SPACING);
            tabRects.add(new Rect(x, tabY, tabW, TAB_HEIGHT, cats[i]));
        }

        int y = tabY + TAB_HEIGHT + SPACING;
        int rowX = panelX + PADDING;
        int rowW = PANEL_WIDTH - PADDING * 2;

        for (Module module : ModuleManager.getModules()) {
            if (module.getCategory() != selected) continue;
            rowRects.add(new Rect(rowX, y, rowW, ROW_HEIGHT, module));
            y += ROW_HEIGHT + SPACING;
        }
    }

    private int totalHeight() {
        int tabsBottom = HEADER_HEIGHT + PADDING + TAB_HEIGHT + SPACING;
        int rowsHeight = rowRects.size() * (ROW_HEIGHT + SPACING);
        return tabsBottom + rowsHeight + PADDING;
    }

    private String categoryLabel(Category c) {
        return switch (c) {
            case COMBAT -> "PVP";
            case MOVEMENT -> "Move";
            case RENDER -> "Render";
            case HUD -> "HUD";
        };
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
        rebuild();
        int totalHeight = totalHeight();

        context.fill(panelX - 1, panelY - 1, panelX + PANEL_WIDTH + 1, panelY + totalHeight + 1, COLOR_PANEL_BORDER);
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + totalHeight, COLOR_PANEL_BG);
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + HEADER_HEIGHT, COLOR_HEADER);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title,
                panelX + PANEL_WIDTH / 2, panelY + 8, 0xFFFFFFFF);

        for (Rect r : tabRects) {
            Category c = (Category) r.data();
            int color = c == selected ? COLOR_TAB_ON : COLOR_TAB_OFF;
            context.fill(r.x(), r.y(), r.x() + r.w(), r.y() + r.h(), color);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(categoryLabel(c)),
                    r.x() + r.w() / 2, r.y() + 6, 0xFFFFFFFF);
        }

        for (Rect r : rowRects) {
            Module m = (Module) r.data();
            context.fill(r.x(), r.y(), r.x() + r.w(), r.y() + r.h(), COLOR_ROW);
            context.drawTextWithShadow(this.textRenderer, labelFor(m), r.x() + 6, r.y() + 6, 0xFFFFFFFF);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseY >= panelY && mouseY <= panelY + HEADER_HEIGHT
                && mouseX >= panelX && mouseX <= panelX + PANEL_WIDTH) {
            dragging = true;
            dragOffsetX = mouseX - panelX;
            dragOffsetY = mouseY - panelY;
            return true;
        }

        for (Rect r : tabRects) {
            if (mouseX >= r.x() && mouseX <= r.x() + r.w()
                    && mouseY >= r.y() && mouseY <= r.y() + r.h()) {
                selected = (Category) r.data();
                return true;
            }
        }

        for (Rect r : rowRects) {
            if (mouseX >= r.x() && mouseX <= r.x() + r.w()
                    && mouseY >= r.y() && mouseY <= r.y() + r.h()) {
                Module m = (Module) r.data();
                if (button == 1) {
                    if (this.client != null) {
                        this.client.setScreen(new ModuleSettingsScreen(m, this));
                    }
                } else {
                    m.toggle();
                    ConfigManager.save();
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            panelX = (int) (mouseX - dragOffsetX);
            panelY = (int) (mouseY - dragOffsetY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
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
