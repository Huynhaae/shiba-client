package com.shiba.client.module.settings;

public class NumberSetting extends Setting {
    private double min, max, value, step;

    public NumberSetting(String name, double min, double max, double defaultValue, double step) {
        super(name);
        this.min = min;
        this.max = max;
        this.value = defaultValue;
        this.step = step;
    }

    public double getValue() { return value; }
    public void setValue(double value) {
        // Ràng buộc và làm tròn theo step
        double clamped = Math.min(max, Math.max(min, value));
        this.value = Math.round(clamped / step) * step;
    }
    public double getMin() { return min; }
    public double getMax() { return max; }
    public double getStep() { return step; }
}
