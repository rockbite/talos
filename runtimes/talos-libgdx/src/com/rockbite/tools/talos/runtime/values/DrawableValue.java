package com.rockbite.tools.talos.runtime.values;

import com.rockbite.tools.talos.runtime.ParticleDrawable;
import com.rockbite.tools.talos.runtime.render.TextureRegionDrawable;

public class DrawableValue extends Value {

    ParticleDrawable drawable;

    public DrawableValue() {
        setEmpty(true);
    }

    @Override
    public void set(Value value) {
        if(value.isEmpty()) {
            setEmpty(true);
            drawable = null;
            return;
        }
        drawable = ((DrawableValue)value).getDrawable();

        setEmpty(drawable == null);
    }

    public ParticleDrawable getDrawable() {
        return drawable;
    }

    public void setDrawable(ParticleDrawable drawable) {
        this.drawable = drawable;
        setEmpty(drawable == null);
    }
}
