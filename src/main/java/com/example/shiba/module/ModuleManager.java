package com.example.shiba.module;

import com.example.shiba.module.impl.CoordsHud;
import com.example.shiba.module.impl.FpsHud;
import com.example.shiba.module.impl.Zoom;
import com.example.shiba.module.impl.Hitbox;
import com.example.shiba.module.impl.Reach;

import java.util.ArrayList;
import java.util.List;

public final class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();

    public static final CoordsHud COORDS = register(new CoordsHud());
    public static final FpsHud FPS = register(new FpsHud());
    public static final Zoom ZOOM = register(new Zoom());
    public static final Hitbox HITBOX = register(new Hitbox());
    public static final Reach REACH = register(new Reach());

    private ModuleManager() {}

    private static <T extends Module> T register(T module) {
        MODULES.add(module);
        return module;
    }

    public static List<Module> getModules() { return MODULES; }

    public static Module byName(String name) {
        return MODULES.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public static void tick() {
        for (Module m : MODULES) {
            if (m.isEnabled()) m.onTick();
        }
    }
}
