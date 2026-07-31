package com.example.shiba.module.impl;

import com.example.shiba.module.Category;
import com.example.shiba.module.Module;

public class ESP extends Module {
    public double range = 5000.0;

    public ESP() {
        super("ESP", "Hien khung entity xuyen tuong.", Category.RENDER);
    }
}
