package com.example.shiba.gui;

import com.example.shiba.module.Module;
import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.Category;
import com.example.shiba.module.settings.Setting;
import com.example.shiba.module.settings.NumberSetting;
import com.example.shiba.module.settings.BooleanSetting;
import com.example.shiba.module.settings.ModeSetting;
import com.example.shiba.module.settings.KeybindSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

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
    private boolean waitingForKeybind = false;
    private Module keybindModule = null;

    // For settings keybind
    private boolean waitingForSettingKeybind = false;
    private Setting keybindSettingInstance = null;

    // Trail points for glow effect
    private final List<int[]> trailPoints = new ArrayList<>();
    private static final int TRAIL_LENGTH = 20;

    // Colors
    private static final int BG_COLOR = 0xFF1A1A1A;
    private static final int PANEL_COLOR = 0xFF2A2A2A;
    private static final int HOVER_COLOR = 0xFF3A3A3A;
    private static final int CATEGORY_SELECTED = 0xFF00AAFF;
    private static final int CATEGORY_UNSELECTED = 0xFF555555;

    public ClickGuiScreen() {
        super(Text.literal("Shiba Client"));
        modules = ModuleManager.getModules();
    }

    @Override
    protected void init() {
        super.init();
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
        this.renderBackground(context, mouseX, mouseY, delta);

        // Update trail
        trailPoints.add(new int[]{mouseX, mouseY});
        if (trailPoints.size() > TRAIL_LENGTH) {
            trailPoints.remove(0);
        }
        drawTrailGlow(context);

        // GUI Components
        drawSearchBar(context);
        drawCategories(context);
        drawModuleList(context);
        drawSettings(context);

        // Keybind overlays
        if (waitingForKeybind || waitingForSettingKeybind) {
            context.fill(0, 0, width, height, 0x88000000);
            String msg = waitingForSettingKeybind ?
                    "Press any key for " + keybindSettingInstance.getName() :
                    "Press any key for " + keybindModule.getName() + " (ESC to cancel)";
            context.drawText(textRenderer, msg, width/2 - textRenderer.getWidth(msg)/2, height/2 - 10, 0xFFFFFF, false);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawTrailGlow(DrawContext context) {
        int size = trailPoints.size();
        if (size < 2) return;
        int glowColor = 0x1E90FF;
        for (int i = 0; i < size; i++) {
            int[] pt = trailPoints.get(i);
            float progress = (float) i / size;
            int alpha = (int)(40 + 100 * progress);
            int radius = 6 + (int)(22 * progress);
            for (int r = radius; r > 0; r--) {
                float p = (float) r / radius;
                int a = (int)(alpha * (1 - p * p));
                if (a <= 0) continue;
                int color = (a << 24) | glowColor;
                context.fill(pt[0] - r, pt[1] - r, pt[0] + r, pt[1] + r, color);
            }
        }
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
            int color = module.isEnabled() ? 0xFF00FF00 : 0xFF888888;
            context.drawText(textRenderer, name, x + 4, y + 5, color, false);

            int keyCode = module.getKeybind();
            String keyName = keyCode == 0 ? "" : GLFW.glfwGetKeyName(keyCode, 0);
            if (keyName == null) keyName = "";
            if (!keyName.isEmpty()) {
                int keyX = x + rowW - textRenderer.getWidth(keyName) - 4;
                context.drawText(textRenderer, keyName, keyX, y + 5, 0xAAAAAA, false);
            }
            y += rowH + spacing;
        }
        if (filtered.isEmpty()) {
            context.drawText(textRenderer, "No modules found", x + 10, y + 10, 0x888888, false);
        }
    }

    private void drawSettings(DrawContext context) {
        if (selectedModule == null) return;
        List<Setting> settings = getSettingsFromModule(selectedModule);
        if (settings.isEmpty()) {
            context.drawText(textRenderer, "No settings", 170, 40, 0x888888, false);
            return;
        }
        int x = 170;
        int y = 40;
        for (Setting setting : settings) {
            if (setting instanceof NumberSetting ns) {
                drawNumberSetting(context, ns, x, y);
                y += 25;
            } else if (setting instanceof BooleanSetting bs) {
                drawBooleanSetting(context, bs, x, y);
                y += 20;
            } else if (setting instanceof ModeSetting ms) {
                drawModeSetting(context, ms, x, y);
                y += 20;
            } else if (setting instanceof KeybindSetting ks) {
                drawKeybindSetting(context, ks, x, y);
                y += 20;
            }
        }
    }

    private void drawNumberSetting(DrawContext context, NumberSetting ns, int x, int y) {
        int w = 120;
        int h = 8;
        double value = ns.getValue();
        double min = ns.getMin();
        double max = ns.getMax();
        double percent = (value - min) / (max - min);
        String text = ns.getName() + ": " + String.format("%.2f", value);
        context.drawText(textRenderer, text, x, y, 0xFFFFFF, false);
        int sliderY = y + 14;
        context.fill(x, sliderY, x + w, sliderY + h, 0xFF333333);
        int fillW = (int)(w * percent);
        context.fill(x, sliderY, x + fillW, sliderY + h, 0xFF00AAFF);
        ns.setSliderX(x);
        ns.setSliderY(sliderY);
        ns.setSliderWidth(w);
        ns.setSliderHeight(h);
    }

    private void drawBooleanSetting(DrawContext context, BooleanSetting bs, int x, int y) {
        String text = bs.getName() + ": " + (bs.getValue() ? "ON" : "OFF");
        context.drawText(textRenderer, text, x, y, bs.getValue() ? 0x00FF00 : 0xFF4444, false);
    }

    private void drawModeSetting(DrawContext context, ModeSetting ms, int x, int y) {
        String text = ms.getName() + ": " + ms.getValue();
        context.drawText(textRenderer, text, x, y, 0xFFFFFF, false);
    }

    private void drawKeybindSetting(DrawContext context, KeybindSetting ks, int x, int y) {
        String text = ks.getName() + ": " + (ks.getValue() == 0 ? "None" : GLFW.glfwGetKeyName(ks.getValue(), 0));
        context.drawText(textRenderer, text, x, y, 0xFFFFFF, false);
    }

    private List<Setting> getSettingsFromModule(Module module) {
        List<Setting> settings = new ArrayList<>();
        try {
            for (var field : module.getClass().getDeclaredFields()) {
                if (Setting.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    settings.add((Setting) field.get(module));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return settings;
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
        if (waitingForKeybind || waitingForSettingKeybind) return false;

        // Click categories
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

        // Click module
        Module clicked = getModuleAt((int) mouseX, (int) mouseY);
        if (clicked != null) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
                if (selectedModule == clicked) {
                    clicked.toggle();
                } else {
                    selectedModule = clicked;
                }
                return true;
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_2) {
                selectedModule = clicked;
                return true;
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_3) {
                startKeybind(clicked);
                return true;
            }
        }

        // Click search box
        if (this.searchBox.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        // Click settings
        if (selectedModule != null && button == GLFW.GLFW_MOUSE_BUTTON_1) {
            handleSettingsClick((int) mouseX, (int) mouseY);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleSettingsClick(int mouseX, int mouseY) {
        if (selectedModule == null) return;
        List<Setting> settings = getSettingsFromModule(selectedModule);
        int x = 170;
        int y = 40;
        for (Setting setting : settings) {
            if (setting instanceof NumberSetting ns) {
                int sx = ns.getSliderX();
                int sy = ns.getSliderY();
                int sw = ns.getSliderWidth();
                int sh = ns.getSliderHeight();
                if (mouseX >= sx && mouseX <= sx + sw &&
                    mouseY >= sy && mouseY <= sy + sh) {
                    double percent = (mouseX - sx) / (double) sw;
                    double newValue = ns.getMin() + (ns.getMax() - ns.getMin()) * percent;
                    ns.setValue(newValue);
                    return;
                }
                y += 25;
            } else if (setting instanceof BooleanSetting bs) {
                String text = bs.getName() + ": " + (bs.getValue() ? "ON" : "OFF");
                int textWidth = textRenderer.getWidth(text);
                if (mouseX >= x && mouseX <= x + textWidth &&
                    mouseY >= y && mouseY <= y + 12) {
                    bs.setValue(!bs.getValue());
                    return;
                }
                y += 20;
            } else if (setting instanceof ModeSetting ms) {
                String text = ms.getName() + ": " + ms.getValue();
                int textWidth = textRenderer.getWidth(text);
                if (mouseX >= x && mouseX <= x + textWidth &&
                    mouseY >= y && mouseY <= y + 12) {
                    List<String> modes = ms.getModes();
                    int currentIndex = modes.indexOf(ms.getValue());
                    int nextIndex = (currentIndex + 1) % modes.size();
                    ms.setValue(modes.get(nextIndex));
                    return;
                }
                y += 20;
            } else if (setting instanceof KeybindSetting ks) {
                String text = ks.getName() + ": " + (ks.getValue() == 0 ? "None" : GLFW.glfwGetKeyName(ks.getValue(), 0));
                int textWidth = textRenderer.getWidth(text);
                if (mouseX >= x && mouseX <= x + textWidth &&
                    mouseY >= y && mouseY <= y + 12) {
                    // Mở chế độ chờ nhấn phím cho keybind setting
                    waitingForSettingKeybind = true;
                    keybindSettingInstance = ks;
                    return;
                }
                y += 20;
            }
        }
    }

    private void startKeybind(Module module) {
        waitingForKeybind = true;
        keybindModule = module;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Xử lý keybind cho module
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

        // Xử lý keybind cho setting
        if (waitingForSettingKeybind) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                waitingForSettingKeybind = false;
                keybindSettingInstance = null;
                return true;
            }
            if (keybindSettingInstance != null) {
                ((KeybindSetting) keybindSettingInstance).setValue(keyCode);
                waitingForSettingKeybind = false;
                keybindSettingInstance = null;
                return true;
            }
        }

        // Ctrl+F để focus search box
        if (keyCode == GLFW.GLFW_KEY_F && isCtrlDown()) {
            this.searchBox.setFocused(true);
            return true;
        }
        if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean isCtrlDown() {
        long handle = MinecraftClient.getInstance().getWindow().getHandle();
        return GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ||
               GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.searchBox.charTyped(chr, modifiers)) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount, double delta) {
        if (waitingForKeybind || waitingForSettingKeybind) return false;
        scrollOffset += amount * 10;
        int maxScroll = Math.max(0, (modules.size() * 24) - (height - 80));
        scrollOffset = Math.max(-maxScroll, Math.min(0, scrollOffset));
        return super.mouseScrolled(mouseX, mouseY, amount, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
