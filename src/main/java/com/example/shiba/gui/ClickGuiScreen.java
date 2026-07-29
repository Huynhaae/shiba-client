package com.example.shiba.gui;

import com.example.shiba.ShibaClient;
import com.example.shiba.config.ConfigManager;
import com.example.shiba.module.Module;
import com.example.shiba.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ClickGuiScreen extends Screen {
    private static final int BUTTON_WIDTH = 160;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SPACING = 4;

    public ClickGuiScreen() {
        super(Text.literal(ShibaClient.MOD_NAME));
    }

    @Override
    protected void init() {
        int x = this.width / 2 - BUTTON_WIDTH / 2;
        int y = 50;

        for (Module module : ModuleManager.getModules()) {
            ButtonWidget button = ButtonWidget.builder(labelFor(module), btn -> {
                module.toggle();
                btn.setMessage(labelFor(module));
                ConfigManager.save();
            }).dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
              .tooltip(Tooltip.of(Text.literal(module.getDescription())))
              .build();

            this.addDrawableChild(button);
            y += BUTTON_HEIGHT + SPACING;
        }
    }

    private Text labelFor(Module module) {
        String state = module.isEnabled() ? "§aBAT" : "§cTAT";
        return Text.literal(module.getName() + ": " + state);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title,
                this.width / 2, 20, 0xFFD9A0);
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
    public boolean shouldPause() { return false; }
}
