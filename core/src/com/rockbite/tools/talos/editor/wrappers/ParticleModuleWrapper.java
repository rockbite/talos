package com.rockbite.tools.talos.editor.wrappers;

import com.rockbite.tools.talos.runtime.Slot;
import com.rockbite.tools.talos.runtime.modules.*;

public class ParticleModuleWrapper extends ModuleWrapper<ParticleModule> {

    @Override
    protected float reportPrefWidth() {
        return 150;
    }

    @Override
    protected void configureSlots() {
        addInputSlot("drawable",  ParticleModule.DRAWABLE);
        addInputSlot("offset",  ParticleModule.OFFSET);
        addInputSlot("life",  ParticleModule.LIFE);

        addSeparator(true);

        addInputSlot("velocity",  ParticleModule.VELOCITY);
        addInputSlot("gravity",  ParticleModule.GRAVITY);
        addInputSlot("rotation",  ParticleModule.ROTATION);
        addInputSlot("target",  ParticleModule.TARGET);
        addInputSlot("color",  ParticleModule.COLOR);
        addInputSlot("transparency",  ParticleModule.TRANSPARENCY);
        addInputSlot("angle",  ParticleModule.ANGLE);
        addInputSlot("mass",  ParticleModule.MASS);
        addInputSlot("size",  ParticleModule.SIZE);
    }

    @Override
    public Class<? extends Module>  getSlotsPreferredModule(Slot slot) {

        if(slot.getIndex() == ParticleModule.OFFSET) return Vector2Module.class;
        if(slot.getIndex() == ParticleModule.TARGET) return Vector2Module.class;
        if(slot.getIndex() == ParticleModule.LIFE) return StaticValueModule.class;
        if(slot.getIndex() == ParticleModule.VELOCITY) return DynamicRangeModule.class;
        if(slot.getIndex() == ParticleModule.ROTATION) return CurveModule.class;
        if(slot.getIndex() == ParticleModule.COLOR) return ColorModule.class;
        if(slot.getIndex() == ParticleModule.TRANSPARENCY) return CurveModule.class;
        if(slot.getIndex() == ParticleModule.ANGLE) return RandomRangeModule.class;
        if(slot.getIndex() == ParticleModule.SIZE) return DynamicRangeModule.class;

        return null;
    }
}
