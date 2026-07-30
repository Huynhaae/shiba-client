package com.example.shiba.module.impl;

import com.example.shiba.module.Category;
import com.example.shiba.module.Module;

public class Zoom extends Module {
    private static final double ZOOM_FOV = 20.0;

    public Zoom() { super("Zoom", "Giu phim de phong to tam nhin", Category.RENDER); }

    public double getFov() { return ZOOM_FOV; }
}
