package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.graphics.Color;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class ColorModule extends Module {

    public static final int R = 0;
    public static final int G = 1;
    public static final int B = 2;
    public static final int OUTPUT = 0;

    NumericalValue r;
    NumericalValue g;
    NumericalValue b;
    NumericalValue output;

    Color tmpColor = new Color();

    float defaultR = 1, defaultG = 0, defaultB = 0;

    @Override
    protected void defineSlots() {
        r = createInputSlot(R);
        g = createInputSlot(G);
        b = createInputSlot(B);

        output = createOutputSlot(OUTPUT);
    }

    @Override
    public void processValues() {

        if(r.isEmpty()) r.set(defaultR);
        if(g.isEmpty()) g.set(defaultG);
        if(b.isEmpty()) b.set(defaultB);

        output.set(r, g, b);
    }

    public void setR(float r) {
        defaultR = r;
    }

    public void setG(float g) {
        defaultG = g;
    }

    public void setB(float b) {
        defaultB = b;
    }

    public Color getColor() {
        tmpColor.set(defaultR, defaultG, defaultB, 1f);
        return tmpColor;
    }
}
