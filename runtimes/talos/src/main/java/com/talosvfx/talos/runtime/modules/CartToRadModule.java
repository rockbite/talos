package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class CartToRadModule extends AbstractModule {

    public static final int POSITION = 0;

    public static final int ANGLE = 0;
    public static final int LENGTH = 1;

    NumericalValue position;
    NumericalValue angle;
    NumericalValue length;

    Vector2 tmp = new Vector2();

    @Override
    protected void defineSlots() {
        position = createInputSlot(POSITION);

        angle = createOutputSlot(ANGLE);
        length = createOutputSlot(LENGTH);
    }

    @Override
    public void processCustomValues () {
        tmp.set(position.get(0), position.get(1));

        angle.set(tmp.angle());
        length.set(tmp.len());
    }

}
