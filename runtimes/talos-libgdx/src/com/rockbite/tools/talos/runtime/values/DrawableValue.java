package com.rockbite.tools.talos.runtime.values;

import com.rockbite.tools.talos.runtime.ParticleDrawable;
import com.rockbite.tools.talos.runtime.render.TextureRegionDrawable;

public class DrawableValue extends Value {

    ParticleDrawable drawable;

    @Override
    public void set(Value value) {
        drawable = ((DrawableValue)value).getDrawable();
    }

    public ParticleDrawable getDrawable() {
        return drawable;
    }

    public void setDrawable(ParticleDrawable drawable) {
        this.drawable = drawable;
    }
}
