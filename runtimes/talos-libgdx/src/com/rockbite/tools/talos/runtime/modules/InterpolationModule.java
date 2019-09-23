package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.runtime.utils.InterpolationMappings;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class InterpolationModule extends Module {

    public static final int ALPHA = 0;
    public static final int OUTPUT = 0;

    NumericalValue alpha;
    NumericalValue output;

    private Interpolation currentInterpolation = Interpolation.linear;

    @Override
    protected void defineSlots() {
        alpha = createInputSlot(ALPHA);

        output = createOutputSlot(OUTPUT);
    }

    @Override
    public void processValues() {
        output.set(currentInterpolation.apply(alpha.getFloat()));
    }

    public void setInterpolation(Interpolation interpolation) {
        this.currentInterpolation = interpolation;
    }

    public Interpolation getInterpolation() {
        return this.currentInterpolation;
    }

    @Override
    public void write (Json json) {
        json.writeValue("interp", InterpolationMappings.getNameForInterpolation(getInterpolation()));
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        currentInterpolation = InterpolationMappings.getInterpolationForName(jsonData.getString("interp"));
    }

}
