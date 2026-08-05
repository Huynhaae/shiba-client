package com.example.shiba.module.settings;

public class NumberSetting extends Setting {
    private double min, max, value, step;
    private int sliderX, sliderY, sliderWidth, sliderHeight;

    public NumberSetting(String name, double min, double max, double defaultValue, double step) {
        super(name);
        this.min = min;
        this.max = max;
        this.value = defaultValue;
        this.step = step;
    }

    public double getValue() { return value; }
    public void setValue(double value) {
        double clamped = Math.min(max, Math.max(min, value));
        this.value = Math.round(clamped / step) * step;
    }
    public double getMin() { return min; }
    public double getMax() { return max; }
    public double getStep() { return step; }

    public void setSliderX(int x) { this.sliderX = x; }
    public void setSliderY(int y) { this.sliderY = y; }
    public void setSliderWidth(int w) { this.sliderWidth = w; }
    public void setSliderHeight(int h) { this.sliderHeight = h; }
    public int getSliderX() { return sliderX; }
    public int getSliderY() { return sliderY; }
    public int getSliderWidth() { return sliderWidth; }
    public int getSliderHeight() { return sliderHeight; }
}
