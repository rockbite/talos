package com.rockbite.tools.talos.runtime.values;

import com.badlogic.gdx.graphics.Color;

public class ColorPoint {
    public float pos;
    public Color color = new Color();

    public ColorPoint() {

    }

    public ColorPoint(Color color, float pos) {
        this.color.set(color);
        this.pos = pos;
    }

    public void set(ColorPoint point) {
        this.pos = point.pos;
        this.color.set(point.color);
    }
}
