package com.example.shiba.gui;

import com.example.shiba.module.Category;
import com.example.shiba.module.Module;
import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.impl.Hitbox;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ClickGuiScreen extends Screen {

    private static final int PANEL_WIDTH = 120;
    private static final int TAB_HEIGHT = 24;
    private static final int MODULE_HEIGHT = 20;
    private static final int SLIDER_HEIGHT = 14;
    private static final int PANEL_GAP = 6;

    private static final int COLOR_BG = 0xCC101014;
    private static final int COLOR_TAB = 0xFF1A1A22;
    private static final int COLOR_TAB_ACTIVE = 0xFF3A6FE0;
    private static final int COLOR_MODULE_OFF = 0xFF1E1E28;
    private static final int COLOR_MODULE_ON = 0xFF2E7D46;
    private static final int COLOR_SLIDER_BG = 0xFF14141C;
    private static final int COLOR_SLIDER_FILL = 0xFF3A6FE0;
    private static final int COLOR_BORDER = 0xFF34344A;
    private static final int COLOR_TEXT = 0xFFE8E8F0;

    private final Map<Category, List<Module>> categorized = new EnumMap<>(Category.class);
    private Category activeCategory = Category.COMBAT;

    private int guiX = 40;
    private int guiY = 40;
    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;

    private boolean draggingSlider = false;

    public ClickGuiScreen() {
        super(Text.literal("Shiba ClickGUI"));
    }

    @Override
    protected void init() {
        categorized.clear();
        for (Category c : Category.values()) {
            categorized.put(c, new ArrayList<>());
        }
        for (Module m : ModuleManager.getModules()) {
            categorized.get(m.getCategory()).add(m);
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx, mouseX, mouseY, delta);

        int panelHeight = tabBarHeight() + PANEL_GAP + moduleListHeight();
        ctx.fill(guiX, guiY, guiX + PANEL_WIDTH, guiY + panelHeight, COLOR_BG);
        ctx.drawBorder(guiX, guiY, PANEL_WIDTH, panelHeight, COLOR_BORDER);

        renderTabs(ctx, mouseX, mouseY);
        renderModules(ctx, mouseX, mouseY);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private int tabBarHeight() {
        return Category.values().length * TAB_HEIGHT;
    }

    private int rowHeight(Module m) {
        return (m instanceof Hitbox hb && hb.isEnabled()) ? MODULE_HEIGHT + SLIDER_HEIGHT : MODULE_HEIGHT;
    }

    private int moduleListHeight() {
        List<Module> mods = categorized.get(activeCategory);
        if (mods.isEmpty()) return MODULE_HEIGHT;
        int total = 0;
        for (Module m : mods) total += rowHeight(m);
        return total;
    }

    private void renderTabs(DrawContext ctx, int mouseX, int mouseY) {
        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            Category c = cats[i];
            int tabY = guiY + i * TAB_HEIGHT;
            boolean active = c == activeCategory;
            boolean hovered = isInside(mouseX, mouseY, guiX, tabY, PANEL_WIDTH, TAB_HEIGHT);

            int color = active ? COLOR_TAB_ACTIVE : (hovered ? 0xFF25253A : COLOR_TAB);
            ctx.fill(guiX, tabY, guiX + PANEL_WIDTH, tabY + TAB_HEIGHT, color);
            ctx.drawText(this.textRenderer, Text.literal(c.name()),
                    guiX + 6, tabY + (TAB_HEIGHT - 8) / 2, COLOR_TEXT, false);
        }
    }

    private void renderModules(DrawContext ctx, int mouseX, int mouseY) {
        List<Module> mods = categorized.get(activeCategory);
        int listY = guiY + tabBarHeight() + PANEL_GAP;

        if (mods.isEmpty()) {
            ctx.drawText(this.textRenderer, Text.literal("(empty)"),
                    guiX + 6, listY + 4, 0xFF888899, false);
            return;
        }

        int rowY = listY;
        for (Module m : mods) {
            boolean hovered = isInside(mouseX, mouseY, guiX, rowY, PANEL_WIDTH, MODULE_HEIGHT);
            int color = m.isEnabled() ? COLOR_MODULE_ON : (hovered ? 0xFF28283A : COLOR_MODULE_OFF);

            ctx.fill(guiX, rowY, guiX + PANEL_WIDTH, rowY + MODULE_HEIGHT - 1, color);
            ctx.drawText(this.textRenderer, Text.literal(m.getName()),
                    guiX + 6, rowY + (MODULE_HEIGHT - 8) / 2, COLOR_TEXT, false);

            if (m instanceof Hitbox hb && hb.isEnabled()) {
                int sliderY = rowY + MODULE_HEIGHT;
                renderExpandSlider(ctx, hb, sliderY);
            }

            rowY += rowHeight(m);
        }
    }

    private void renderExpandSlider(DrawContext ctx, Hitbox hb, int sliderY) {
        ctx.fill(guiX, sliderY, guiX + PANEL_WIDTH, sliderY + SLIDER_HEIGHT - 1, COLOR_SLIDER_BG);

        double ratio = (hb.getExpand() - Hitbox.MIN_EXPAND) / (Hitbox.MAX_EXPAND - Hitbox.MIN_EXPAND);
        int fillWidth = (int) (PANEL_WIDTH * ratio);
        ctx.fill(guiX, sliderY, guiX + fillWidth, sliderY + SLIDER_HEIGHT - 1, COLOR_SLIDER_FILL);

        String label = String.format("expand: %.2f", hb.getExpand());
        ctx.drawText(this.textRenderer, Text.literal(label),
                guiX + 4, sliderY + 2, COLOR_TEXT, false);
    }

    private boolean isOverExpandSlider(double mouseX, double mouseY, int[] outSliderY) {
        List<Module> mods = categorized.get(activeCategory);
        int rowY = guiY + tabBarHeight() + PANEL_GAP;

        for (Module m : mods) {
            if (m instanceof Hitbox hb && hb.isEnabled()) {
                int sliderY = rowY + MODULE_HEIGHT;
                if (isInside(mouseX, mouseY, guiX, sliderY, PANEL_WIDTH, SLIDER_HEIGHT)) {
                    outSliderY[0] = sliderY;
                    return true;
                }
            }
            rowY += rowHeight(m);
        }
        return false;
    }

    private void applyExpandFromMouse(double mouseX) {
        List<Module> mods = categorized.get(activeCategory);
        for (Module m : mods) {
            if (m instanceof Hitbox hb && hb.isEnabled()) {
                double ratio = (mouseX - guiX) / (double) PANEL_WIDTH;
                ratio = Math.max(0.0, Math.min(1.0, ratio));
                double value = Hitbox.MIN_EXPAND + ratio * (Hitbox.MAX_EXPAND - Hitbox.MIN_EXPAND);
                hb.setExpand(value);
                return;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int[] sliderY = new int[1];
            if (isOverExpandSlider(mouseX, mouseY, sliderY)) {
                draggingSlider = true;
                applyExpandFromMouse(mouseX);
                return true;
            }

            if (isInside(mouseX, mouseY, guiX, guiY, PANEL_WIDTH, TAB_HEIGHT)) {
                dragging = true;
                dragOffsetX = (int) mouseX - guiX;
                dragOffsetY = (int) mouseY - guiY;
            }

            Category[] cats = Category.values();
            for (int i = 0; i < cats.length; i++) {
                int tabY = guiY + i * TAB_HEIGHT;
                if (isInside(mouseX, mouseY, guiX, tabY, PANEL_WIDTH, TAB_HEIGHT)) {
                    activeCategory = cats[i];
                    return true;
                }
            }

            List<Module> mods = categorized.get(activeCategory);
            int rowY = guiY + tabBarHeight() + PANEL_GAP;
            for (Module m : mods) {
                if (isInside(mouseX, mouseY, guiX, rowY, PANEL_WIDTH, MODULE_HEIGHT)) {
                    m.toggle();
                    return true;
                }
                rowY += rowHeight(m);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingSlider) {
            applyExpandFromMouse(mouseX);
            return true;
        }
        if (dragging) {
            guiX = (int) mouseX - dragOffsetX;
            guiY = (int) mouseY - dragOffsetY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        draggingSlider = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private boolean isInside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
