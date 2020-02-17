package com.talosvfx.talos.runtime.modules;

import com.talosvfx.talos.runtime.render.drawables.ShaderDrawable;
import com.talosvfx.talos.runtime.values.DrawableValue;
import com.talosvfx.talos.runtime.values.Value;

public class ShaderModule extends AbstractModule {

    public static final int COLOR = 0;

    public static final int OUTPUT = 0;

    private DrawableValue outputValue;

    @Override
    protected void defineSlots() {
        createInputSlot(ShaderModule.COLOR);

        outputValue = (DrawableValue) createOutputSlot(OUTPUT, new DrawableValue());
        outputValue.setDrawable(new ShaderDrawable());
    }

    @Override
    public Value fetchOutputSlotValue(int slotId) {

        fetchOutputShaderCode();
        processValues();
        return outputSlots.get(slotId).getValue();
    }

    @Override
    public void processValues() {
        ((ShaderDrawable)outputValue.getDrawable()).setCode(shaderCode);
    }
}
