package dev.flrp.economobs.util;

public class MinMax {

    private double min;
    private double max;

    public MinMax(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public boolean isBetween(double value) {
        return value >= min && value <= max;
    }

    public double calculateNumber(boolean allowDecimals) {
        return min + (Math.random() * (max - min + (allowDecimals ? 0 : 1)));
    }

}
