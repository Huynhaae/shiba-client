package com.example.shiba.gui;

import com.example.shiba.ShibaClient;
import com.example.shiba.module.Module;
import com.example.shiba.module.impl.Hitbox;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ClickGuiScreen extends Screen {
    public ClickGuiScreen() {
        super(Text.literal("Shiba Client GUI"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int y = 40;
        
        for (Module module : ShibaClient.moduleManager.getModules()) {
            // Sửa isToggled() thành isEnabled()
            context.fill(50, y, 150, y + 15, module.isEnabled() ? 0xFF00AA00 : 0xFFAA0000);
            context.drawText(this.client.textRenderer, module.getName(), 55, y + 4, 0xFFFFFFFF, false);
            
            // Vẽ thanh Slider nếu là Hitbox và đang bật
            if (module.getName().equals("Hitbox") && module.isEnabled()) {
                int sX = 50, sY = y + 15;
                context.fill(sX, sY, sX + 100, sY + 10, 0xFF555555);
                
                float percent = (Hitbox.expand - Hitbox.MIN_EXPAND) / (Hitbox.MAX_EXPAND - Hitbox.MIN_EXPAND);
                context.fill(sX, sY, sX + (int)(100 * percent), sY + 10, 0xFFAAAAFF);
                context.drawText(this.client.textRenderer, "Expand: " + String.format("%.2f", Hitbox.expand), sX + 2, sY + 1, 0xFFFFFFFF, false);
                y += 10; 
            }
            y += 20;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int y = 40;
        for (Module module : ShibaClient.moduleManager.getModules()) {
            if (mouseX >= 50 && mouseX <= 150 && mouseY >= y && mouseY <= y + 15) {
                module.toggle();
                return true;
            }
            if (module.getName().equals("Hitbox") && module.isEnabled()) {
                int sX = 50, sY = y + 15;
                if (mouseX >= sX && mouseX <= sX + 100 && mouseY >= sY && mouseY <= sY + 10) {
                    updateHitboxSlider(mouseX, sX);
                    return true;
                }
                y += 10;
            }
            y += 20;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int y = 40;
        for (Module module : ShibaClient.moduleManager.getModules()) {
            if (module.getName().equals("Hitbox") && module.isEnabled()) {
                int sX = 50, sY = y + 15;
                if (mouseX >= sX && mouseX <= sX + 100 && mouseY >= sY && mouseY <= sY + 10) {
                    updateHitboxSlider(mouseX, sX);
                    return true;
                }
                y += 10;
            }
            y += 20;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    private void updateHitboxSlider(double mouseX, int sX) {
        float percent = (float) (mouseX - sX) / 100.0f;
        Hitbox.expand = Hitbox.MIN_EXPAND + percent * (Hitbox.MAX_EXPAND - Hitbox.MIN_EXPAND);
        if (Hitbox.expand < Hitbox.MIN_EXPAND) Hitbox.expand = Hitbox.MIN_EXPAND;
        if (Hitbox.expand > Hitbox.MAX_EXPAND) Hitbox.expand = Hitbox.MAX_EXPAND;
    }
}
