package com.example.shiba.gui;

import com.example.shiba.module.Module;
import com.example.shiba.module.ModuleManager;
import com.example.shiba.module.Category;
import com.example.shiba.module.settings.NumberSetting;
import com.example.shiba.module.settings.ModeSetting;
import com.example.shiba.module.settings.BooleanSetting;
import com.example.shiba.module.settings.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClickGuiScreen extends Screen {
    private final List<Module> modules;
    private Category selected = Category.COMBAT;
    private Module selectedModule = null;
    private int scrollY = 0;
    private int mouseX, mouseY;

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

        int x = 10;
        int y = 35 + scrollY;
        int rowW = 120;
        int rowH = 22;
        int spacing = 2;

        List<Module> visibleModules = modules.stream()
                .filter(m -> m.getCategory() == selected)
                .collect(Collectors.toList());

        for (Module module : visibleModules) {
            boolean hovered = mouseX >= x && mouseX <= x + rowW &&
                              mouseY >= y && mouseY <= y + rowH;
            int bgColor = (module == selectedModule) ? 0xFF444466 :
                          hovered ? 0xFF555577 : 0xFF333355;
            context.fill(x, y, x + rowW, y + rowH, bgColor);

            String name = module.getName();
            int color = module.isEnabled() ? 0x00FF00 : 0xFF8888;
            context.drawText(textRenderer, name, x + 4, y + 5, color, false);

            y += rowH + spacing;
        }

        if (selectedModule != null) {
            context.drawText(textRenderer, selectedModule.getName() + " Settings", x + rowW + 20, 25, 0xFFFFFF, false);
            drawSettings(context, selectedModule, x + rowW + 20, 35);
        }

        super.render(context, mouseX, mouseY, delta);
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
                y += 25;
            } else if (setting instanceof ModeSetting ms) {
                drawModeSetting(context, ms, x, y);
                y += 25;
            } else if (setting instanceof BooleanSetting bs) {
                drawBooleanSetting(context, bs, x, y);
                y += 25;
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
        int h = 12;
        double value = ns.getValue();
        double min = ns.getMin();
        double max = ns.getMax();
        double percent = (value - min) / (max - min);

        String text = ns.getName() + ": " + String.format("%.2f", value);
        context.drawText(textRenderer, text, x, y - 2, 0xFFFFFF, false);

        int sliderY = y + 12;
        context.fill(x, sliderY, x + w, sliderY + h, 0xFF333333);
        int fillW = (int)(w * percent);
        context.fill(x, sliderY, x + fillW, sliderY + h, 0xFF00AA00);

        if (mouseX >= x && mouseX <= x + w &&
            mouseY >= sliderY && mouseY <= sliderY + h) {
            double newPercent = (mouseX - x) / (double) w;
            double newValue = min + (max - min) * newPercent;
            ns.setValue(newValue);
        }
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
        int x = 10;
        int y = 35 + scrollY;
        int rowW = 120;
        int rowH = 22;
        int spacing = 2;

        List<Module> visibleModules = modules.stream()
                .filter(m -> m.getCategory() == selected)
                .collect(Collectors.toList());

        for (Module module : visibleModules) {
            if (mouseX >= x && mouseX <= x + rowW &&
                mouseY >= y && mouseY <= y + rowH) {
                if (button == 0) {
                    if (selectedModule == module) {
                        module.toggle();
                    } else {
                        selectedModule = module;
                    }
                    return true;
                }
            }
            y += rowH + spacing;
        }

        if (selectedModule != null) {
            handleSettingsClick(selectedModule, (int) mouseX, (int) mouseY, button);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleSettingsClick(Module module, int mouseX, int mouseY, int button) {
        List<Setting> settings = getSettingsFromModule(module);
        int x = 140;
        int y = 55;

        for (Setting setting : settings) {
            if (setting instanceof NumberSetting ns) {
                int w = 150;
                int h = 12;
                int sliderY = y + 22;
                if (mouseX >= x && mouseX <= x + w &&
                    mouseY >= sliderY && mouseY <= sliderY + h) {
                    double percent = (mouseX - x) / (double) w;
                    double newValue = ns.getMin() + (ns.getMax() - ns.getMin()) * percent;
                    ns.setValue(newValue);
                    return;
                }
                y += 35;
            } else if (setting instanceof BooleanSetting bs) {
                String text = bs.getName() + ": " + (bs.getValue() ? "ON" : "OFF");
                int textWidth = textRenderer.getWidth(text);
                if (mouseX >= x && mouseX <= x + textWidth &&
                    mouseY >= y && mouseY <= y + 12) {
                    bs.setValue(!bs.getValue());
                    return;
                }
                y += 25;
            } else if (setting instanceof ModeSetting ms) {
                // Xử lý click chọn mode (có thể thêm nút < >)
                y += 25;
            }
        }
    }

    // SỬA QUAN TRỌNG: mouseScrolled có 4 tham số trong 1.21.1
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount, double delta) {
        scrollY += amount * 10;
        return super.mouseScrolled(mouseX, mouseY, amount, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
