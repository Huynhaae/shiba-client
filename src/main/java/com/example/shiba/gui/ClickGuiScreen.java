package com.example.shiba.gui;

import com.example.shiba.module.Module;
import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.Category;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClickGuiScreen extends Screen {
    private final List<Module> modules;
    private Category selectedCategory = Category.COMBAT;
    private Module selectedModule = null;
    private int scrollOffset = 0;
    private int mouseX, mouseY;
    private TextFieldWidget searchBox;
    private String searchQuery = "";

    // Màu sắc
    private static final int BG_COLOR = 0xFF1A1A1A;
    private static final int PANEL_COLOR = 0xFF2A2A2A;
    private static final int HOVER_COLOR = 0xFF3A3A3A;
    private static final int CATEGORY_SELECTED = 0xFF00AAFF;
    private static final int CATEGORY_UNSELECTED = 0xFF555555;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int ENABLED_COLOR = 0xFF00FF00;
    private static final int DISABLED_COLOR = 0xFF888888;

    // Glow circle
    private static final int GLOW_RADIUS = 25;
    private static final int GLOW_COLOR = 0x0088FF; // Xanh dương phát sáng

    public ClickGuiScreen() {
        super(Text.literal("Shiba Client"));
        modules = ModuleManager.getModules();
    }

    @Override
    protected void init() {
        super.init();
        // Thanh tìm kiếm
        this.searchBox = new TextFieldWidget(textRenderer, 10, 10, 150, 18, Text.literal("Search..."));
        this.searchBox.setMaxLength(50);
        this.searchBox.setDrawsBackground(true);
        this.searchBox.setEditableColor(0xFFFFFF);
        this.searchBox.setUneditableColor(0x888888);
        this.searchBox.setChangedListener(this::onSearchChanged);
        this.addSelectableChild(this.searchBox);
    }

    private void onSearchChanged(String newText) {
        this.searchQuery = newText;
        this.scrollOffset = 0;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        // Nền đen
        this.renderBackground(context, mouseX, mouseY, delta);

        // Vẽ glow circle theo chuột (vòng tròn phát sáng)
        drawGlowCircle(context, mouseX, mouseY);

        // Vẽ các thành phần GUI
        drawSearchBar(context);
        drawCategories(context);
        drawModuleList(context);

        // Keybind overlay
        if (waitingForKeybind) {
            context.fill(0, 0, width, height, 0x88000000);
            String msg = "Press any key for " + keybindModule.getName() + " (ESC to cancel)";
            context.drawText(textRenderer, msg, width/2 - textRenderer.getWidth(msg)/2, height/2 - 10, 0xFFFFFF, false);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawGlowCircle(DrawContext context, int x, int y) {
        // Vẽ nhiều lớp để tạo hiệu ứng phát sáng
        for (int r = GLOW_RADIUS; r > 0; r--) {
            float progress = (float) r / GLOW_RADIUS;
            int alpha = (int)(80 * (1 - progress * progress));
            if (alpha <= 0) continue;
            int color = (alpha << 24) | GLOW_COLOR;
            context.fill(x - r, y - r, x + r, y + r, color);
        }
        // Vẽ outline trắng mờ
        int outlineColor = (30 << 24) | 0xFFFFFF;
        context.fill(x - GLOW_RADIUS - 2, y - GLOW_RADIUS - 2, x + GLOW_RADIUS + 2, y + GLOW_RADIUS + 2, outlineColor);
    }

    private void drawSearchBar(DrawContext context) {
        this.searchBox.render(context, mouseX, mouseY, 0);
    }

    private void drawCategories(DrawContext context) {
        int x = 170;
        int y = 10;
        int catWidth = 80;
        int catHeight = 18;

        for (Category cat : Category.values()) {
            boolean selected = (cat == selectedCategory);
            int bgColor = selected ? CATEGORY_SELECTED : CATEGORY_UNSELECTED;
            context.fill(x, y, x + catWidth, y + catHeight, bgColor);

            String label = cat.name();
            int color = selected ? 0xFFFFFF : 0xAAAAAA;
            context.drawText(textRenderer, label, x + 5, y + 4, color, false);

            // Click xử lý trong mouseClicked
            x += catWidth + 2;
        }
    }

    private void drawModuleList(DrawContext context) {
        int x = 10;
        int y = 40 + scrollOffset;
        int rowW = 150;
        int rowH = 22;
        int spacing = 2;

        List<Module> filtered = modules.stream()
                .filter(m -> m.getCategory() == selectedCategory)
                .filter(m -> searchQuery.isEmpty() || m.getName().toLowerCase().contains(searchQuery.toLowerCase()))
                .collect(Collectors.toList());

        for (Module module : filtered) {
            boolean hovered = mouseX >= x && mouseX <= x + rowW &&
                              mouseY >= y && mouseY <= y + rowH;

            int bgColor = (module == selectedModule) ? 0xFF444466 :
                          hovered ? HOVER_COLOR : PANEL_COLOR;
            context.fill(x, y, x + rowW, y + rowH, bgColor);

            String name = module.getName();
            int color = module.isEnabled() ? ENABLED_COLOR : DISABLED_COLOR;
            context.drawText(textRenderer, name, x + 4, y + 5, color, false);

            // Hiển thị keybind (nếu có)
            int keyCode = module.getKeybind();
            String keyName = keyCode == 0 ? "" : GLFW.glfwGetKeyName(keyCode, 0);
            if (keyName == null) keyName = "";
            if (!keyName.isEmpty()) {
                int keyX = x + rowW - textRenderer.getWidth(keyName) - 4;
                context.drawText(textRenderer, keyName, keyX, y + 5, 0xAAAAAA, false);
            }

            y += rowH + spacing;
        }

        // Nếu không có module nào hiển thị
        if (filtered.isEmpty()) {
            context.drawText(textRenderer, "No modules found", x + 10, y + 10, 0x888888, false);
        }
    }

    private Module getModuleAt(int mouseX, int mouseY) {
        int x = 10;
        int y = 40 + scrollOffset;
        int rowW = 150;
        int rowH = 22;
        int spacing = 2;

        List<Module> filtered = modules.stream()
                .filter(m -> m.getCategory() == selectedCategory)
                .filter(m -> searchQuery.isEmpty() || m.getName().toLowerCase().contains(searchQuery.toLowerCase()))
                .collect(Collectors.toList());

        for (Module module : filtered) {
            if (mouseX >= x && mouseX <= x + rowW &&
                mouseY >= y && mouseY <= y + rowH) {
                return module;
            }
            y += rowH + spacing;
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (waitingForKeybind) return false;

        // Xử lý click vào category
        int x = 170;
        int y = 10;
        int catWidth = 80;
        int catHeight = 18;
        for (Category cat : Category.values()) {
            if (mouseX >= x && mouseX <= x + catWidth &&
                mouseY >= y && mouseY <= y + catHeight) {
                selectedCategory = cat;
                selectedModule = null;
                scrollOffset = 0;
                return true;
            }
            x += catWidth + 2;
        }

        // Xử lý click vào module
        Module clicked = getModuleAt((int) mouseX, (int) mouseY);
        if (clicked != null) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_1) { // Trái
                if (selectedModule == clicked) {
                    clicked.toggle();
                } else {
                    selectedModule = clicked;
                }
                return true;
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_2) { // Phải
                selectedModule = clicked;
                return true;
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_3) { // Giữa
                startKeybind(clicked);
                return true;
            }
        }

        // Xử lý click vào search box
        if (this.searchBox.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount, double delta) {
        if (waitingForKeybind) return false;
        scrollOffset += amount * 10;
        // Giới hạn cuộn
        int maxScroll = Math.max(0, (modules.size() * 24) - (height - 80));
        scrollOffset = Math.max(-maxScroll, Math.min(0, scrollOffset));
        return super.mouseScrolled(mouseX, mouseY, amount, delta);
    }

    // Keybind logic
    private boolean waitingForKeybind = false;
    private Module keybindModule = null;

    private void startKeybind(Module module) {
        waitingForKeybind = true;
        keybindModule = module;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (waitingForKeybind) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                waitingForKeybind = false;
                keybindModule = null;
                return true;
            }
            if (keybindModule != null) {
                keybindModule.setKeybind(keyCode);
                waitingForKeybind = false;
                keybindModule = null;
                return true;
            }
        }
        // Xử lý phím tắt tìm kiếm (Ctrl+F)
        if (keyCode == GLFW.GLFW_KEY_F && isCtrlDown()) {
            this.searchBox.setFocused(true);
            return true;
        }
        // Truyền key vào search box
        if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean isCtrlDown() {
        return GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ||
               GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.searchBox.charTyped(chr, modifiers)) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
