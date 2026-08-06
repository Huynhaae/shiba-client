package com.example.shiba.module;

import com.example.shiba.module.impl.XrayX;
import com.example.shiba.module.impl.AimX;
import com.example.shiba.module.impl.Aura;
import com.example.shiba.module.impl.CoordsHud;
import com.example.shiba.module.impl.CritDisplay;
import com.example.shiba.module.impl.CritX;
import com.example.shiba.module.impl.ESP;
import com.example.shiba.module.impl.FpsHud;
import com.example.shiba.module.impl.Hitbox;
import com.example.shiba.module.impl.HitboxBV;
import com.example.shiba.module.impl.MaceX;
import com.example.shiba.module.impl.Reach;
import com.example.shiba.module.impl.TriggerBot;
import com.example.shiba.module.impl.WTap;
import com.example.shiba.module.impl.Zoom;

import java.util.ArrayList;
import java.util.List;

public final class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();

    public static final XrayX XRAYX = register(new XrayX());
    public static final CoordsHud COORDS = register(new CoordsHud());
    public static final FpsHud FPS = register(new FpsHud());
    public static final Zoom ZOOM = register(new Zoom());
    public static final Hitbox HITBOX = register(new Hitbox());
    public static final Reach REACH = register(new Reach());
    public static final TriggerBot TRIGGERBOT = register(new TriggerBot());
    public static final ESP ESP = register(new ESP());
    public static final Aura AURA = register(new Aura());
    public static final HitboxBV HITBOXBV = register(new HitboxBV());
    public static final CritX CRITX = register(new CritX());
    public static final AimX AIMX = register(new AimX());
    public static final MaceX MACEX = register(new MaceX());
    public static final WTap WTAP = register(new WTap());
    public static final CritDisplay CRITDISPLAY = register(new CritDisplay());

    private ModuleManager() {}

    private static <T extends Module> T register(T module) {
        MODULES.add(module);
        return module;
    }

    public static List<Module> getModules() {
        return MODULES;
    }

    public static Module byName(String name) {
        return MODULES.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public static void tick() {
        for (Module m : MODULES) {
            if (m.isEnabled()) m.onTick();
        }
    }
}
