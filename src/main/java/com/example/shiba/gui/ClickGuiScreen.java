package com.example.shiba.gui;

import com.example.shiba.module.Module;
import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.Category;
import com.example.shiba.module.settings.NumberSetting;
import com.example.shiba.module.settings.ModeSetting;
import com.example.shiba.module.settings.BooleanSetting;
import com.example.shiba.module.settings.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClickGuiScreen extends Screen {
    private final List<Module> modules;
    private Category selected = Category.COMBAT;
    private Module selectedModule = null;
    private int scrollY = 0;
    private int mouseX, mouseY;
    private boolean waitingForKeybind = false;
    private Module keybindModule = null;

    // Danh sách vị trí chuột để tạo hiệu ứng đuôi
    private final List<int[]> trailPoints = new ArrayList<>();
    private static final int TRAIL_LENGTH = 20;

    private static final int BG_COLOR = 0xFF1A1A1A;
    private static final int PANEL_COLOR = 0xFF2A2A2A;
    private static final int HOVER_COLOR = 0xFF3A3A3A;

    public ClickGuiScreen() {
        super(Text.literal("Shiba Client"));
        modules = ModuleManager.getModules();
    }

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), button -> {
            if (this.client != null) this.client.setScreen(null);
        }).dimensions(5, 5, 50, 20).build());

        int x = 60;
        for (Category c : Category.values()) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal(c.name()), button -> {
                selected = c;
                selectedModule = null;
                scrollY = 0;
            }).dimensions(x, 5, 60, 20).build());
            x += 65;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.renderBackground(context, mouseX, mouseY, delta);

        // Cập nhật trail points
        trailPoints.add(new int[]{mouseX, mouseY});
        if (trailPoints.size() > TRAIL_LENGTH) {
            trailPoints.remove(0);
        }

        // Vẽ hiệu ứng đuôi mờ (blur animation)
        drawTrailGlow(context);

        int x = 10;
        int y = 35 + scrollY;
        int rowW = 140;
        int rowH = 24;
        int spacing = 2;

        List<Module> visibleModules = modules.stream()
                .filter(m -> m.getCategory() == selected)
                .collect(Collectors.toList());

        for (Module module : visibleModules) {
            boolean hovered = mouseX >= x && mouseX <= x + rowW &&
                              mouseY >= y && mouseY <= y + rowH;

            int bgColor = (module == selectedModule) ? 0xFF444466 :
                          hovered ? HOVER_COLOR : PANEL_COLOR;
            context.fill(x, y, x + rowW, y + rowH, bgColor);

            String name = module.getName();
            int color = module.isEnabled() ? 0xFF00FF00 : 0xFF888888;
            context.drawText(textRenderer, name, x + 6, y + 6, color, false);

            int keyCode = module.getKeybind();
            String keyName = keyCode == 0 ? "None" : GLFW.glfwGetKeyName(keyCode, 0);
            if (keyName == null) keyName = "Unknown";
            int keyX = x + rowW - textRenderer.getWidth(keyName) - 6;
            context.drawText(textRenderer, keyName, keyX, y + 6, 0xFFAAAAAA, false);

            y += rowH + spacing;
        }

        if (selectedModule != null) {
            int settingsX = x + rowW + 20;
            context.drawText(textRenderer, selectedModule.getName() + " Settings", settingsX, 25, 0xFFFFFF, false);
            drawSettings(context, selectedModule, settingsX, 45);
        }

        if (waitingForKeybind) {
            context.fill(0, 0, width, height, 0x88000000);
            String msg = "Press any key for " + keybindModule.getName() + " (ESC to cancel)";
            context.drawText(textRenderer, msg, width/2 - textRenderer.getWidth(msg)/2, height/2 - 10, 0xFFFFFF, false);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    // Hiệu ứng đuôi mờ dần (blur trail)
    private void drawTrailGlow(DrawContext context) {
        int size = trailPoints.size();
        if (size < 2) return;

        for (int i = 0; i < size; i++) {
            int[] pt = trailPoints.get(i);
            // Độ trong suốt tăng dần theo thời gian (i càng gần cuối càng đậm)
            float progress = (float) i / size; // 0 -> 1
            float alpha = 20 + 60 * progress; // 20 -> 80
            int radius = 30 + (int)(20 * progress); // 30 -> 50

            int color = ((int)alpha << 24) | 0xFFFFFF;
            context.fill(pt[0] - radius, pt[1] - radius, pt[0] + radius, pt[1] + radius, color);
        }
    }

    private void drawSettings(DrawContext context, Module module, int x, int y) {
        List<Setting> settings = getSettingsFromModule(module);
        if (settings.isEmpty()) {
            context.drawText(textRenderer, "No settings", x, y, 0x888888, false);
            return;
        }

        for (Setting setting : settings) {
            if (setting instanceof NumberSetting ns) {
                drawNumberSetting(context, ns, x, y);
                y += 30;
            } else if (setting instanceof ModeSetting ms) {
                drawModeSetting(context, ms, x, y);
                y += 22;
            } else if (setting instanceof BooleanSetting bs) {
                drawBooleanSetting(context, bs, x, y);
                y += 22;
            }
        }
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

    private void drawNumberSetting(DrawContext context, NumberSetting ns, int x, int y) {
        int w = 150;
        int h = 10;
        double value = ns.getValue();
        double min = ns.getMin();
        double max = ns.getMax();
        double percent = (value - min) / (max - min);

        String text = ns.getName() + ": " + String.format("%.2f", value);
        context.drawText(textRenderer, text, x, y, 0xFFFFFF, false);

        int sliderY = y + 14;
        context.fill(x, sliderY, x + w, sliderY + h, 0xFF444444);
        int fillW = (int)(w * percent);
        context.fill(x, sliderY, x + fillW, sliderY + h, 0xFF00AA00);

        ns.setSliderX(x);
        ns.setSliderY(sliderY);
        ns.setSliderWidth(w);
        ns.setSliderHeight(h);
    }

    private void drawModeSetting(DrawContext context, ModeSetting ms, int x, int y) {
        String text = ms.getName() + ": " + ms.getValue();
        context.drawText(textRenderer, text, x, y, 0xFFFFFF, false);
    }

    private void drawBooleanSetting(DrawContext context, BooleanSetting bs, int x, int y) {
        String text = bs.getName() + ": " + (bs.getValue() ? "ON" : "OFF");
        context.drawText(textRenderer, text, x, y, bs.getValue() ? 0x00FF00 : 0xFF4444, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (waitingForKeybind) return false;

        int x = 10;
        int y = 35 + scrollY;
        int rowW = 140;
        int rowH = 24;
        int spacing = 2;

        List<Module> visibleModules = modules.stream()
                .filter(m -> m.getCategory() == selected)
                .collect(Collectors.toList());

        for (Module module : visibleModules) {
            if (mouseX >= x && mouseX <= x + rowW &&
                mouseY >= y && mouseY <= y + rowH) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
                    if (selectedModule == module) {
                        module.toggle();
                    } else {
                        selectedModule = module;
                    }
                    return true;
                } else if (button == GLFW.GLFW_MOUSE_BUTTON_2) {
                    selectedModule = module;
                    return true;
                } else if (button == GLFW.GLFW_MOUSE_BUTTON_3) {
                    waitingForKeybind = true;
                    keybindModule = module;
                    return true;
                }
            }
            y += rowH + spacing;
        }

        if (selectedModule != null && button == GLFW.GLFW_MOUSE_BUTTON_1) {
            handleSettingsClick(selectedModule, (int) mouseX, (int) mouseY);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleSettingsClick(Module module, int mouseX, int mouseY) {
        List<Setting> settings = getSettingsFromModule(module);
        int x = 160;
        int y = 55;

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
                y += 30;
            } else if (setting instanceof BooleanSetting bs) {
                String text = bs.getName() + ": " + (bs.getValue() ? "ON" : "OFF");
                int textWidth = textRenderer.getWidth(text);
                if (mouseX >= x && mouseX <= x + textWidth &&
                    mouseY >= y && mouseY <= y + 12) {
                    bs.setValue(!bs.getValue());
                    return;
                }
                y += 22;
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
                y += 22;
            }
        }
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
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount, double delta) {
        if (!waitingForKeybind) scrollY += amount * 10;
        return super.mouseScrolled(mouseX, mouseY, amount, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
