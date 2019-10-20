package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.math.Vector2;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

/**
 * Calculates the position, rotatoin and length of particle beam based on from and to vector values
 */
public class FromToModule extends Module {

    public static final int FROM = 0;
    public static final int TO = 1;

    public static final int POSITION = 0;
    public static final int ROTATION = 1;
    public static final int LENGTH = 2;

    public NumericalValue from;
    public NumericalValue to;

    NumericalValue position;
    NumericalValue rotation;
    NumericalValue length;

    Vector2 tmpVec = new Vector2();

    public Vector2 defaultFrom = new Vector2();
    public Vector2 defaultTo = new Vector2();

    @Override
    protected void defineSlots() {
        from = createInputSlot(FROM);
        to = createInputSlot(TO);

        position = createOutputSlot(POSITION);
        rotation = createOutputSlot(ROTATION);
        length = createOutputSlot(LENGTH);
    }

    @Override
    public void processValues() {
        if(from.isEmpty()) from.set(defaultFrom.x, defaultFrom.y);
        if(to.isEmpty()) to.set(defaultTo.x, defaultTo.y);

        tmpVec.set(to.get(0), to.get(1));
        tmpVec.sub(from.get(0), from.get(1));

        position.set(from.get(0) + tmpVec.x/2f, from.get(1) + tmpVec.y/2f);

        rotation.set(tmpVec.angle());

        length.set(tmpVec.len());
    }

    public void setDefaults(Vector2 dFrom, Vector2 dTo) {
        defaultFrom.set(dFrom);
        defaultTo.set(dTo);
    }
}
