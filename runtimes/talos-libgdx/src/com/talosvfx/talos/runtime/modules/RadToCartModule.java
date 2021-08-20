package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class RadToCartModule extends AbstractModule {

    public static final int A = 0;
    public static final int L = 1;
    public static final int OUTPUT = 0;

    NumericalValue a;
    NumericalValue l;
    NumericalValue output;

    Vector2 tmp = new Vector2();

    @Override
    protected void defineSlots() {
        a = createInputSlot(A);
        l = createInputSlot(L);

        output = createOutputSlot(OUTPUT);

        a.setFlavour(NumericalValue.Flavour.ANGLE);
    }

    @Override
    public void processCustomValues () {
        tmp.set(l.getFloat(), 0);
        tmp.rotate(a.getFloat());

        output.set(tmp.x, tmp.y);
    }
}
