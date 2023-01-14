package com.talosvfx.talos.runtime.modules;

import com.talosvfx.talos.runtime.values.NumericalValue;

public class VectorSplitModule extends AbstractModule {

    public static final int INPUT = 0;
    public static final int X_OUT = 1;
    public static final int Y_OUT = 2;
    public static final int Z_OUT = 3;

    NumericalValue input;

    NumericalValue xOut;
    NumericalValue yOut;
    NumericalValue zOut;


    @Override
    protected void defineSlots() {
        input = createInputSlot(INPUT);

        xOut = createOutputSlot(X_OUT);
        yOut = createOutputSlot(Y_OUT);
        zOut = createOutputSlot(Z_OUT);
    }

    @Override
    public void processCustomValues () {
        xOut.set(input.get(0));
        yOut.set(input.get(1));
        zOut.set(input.get(2));
    }
}
