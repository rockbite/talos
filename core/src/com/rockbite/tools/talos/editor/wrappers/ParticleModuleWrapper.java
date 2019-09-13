package com.rockbite.tools.talos.editor.wrappers;

import com.rockbite.tools.talos.runtime.EmitterModule;
import com.rockbite.tools.talos.runtime.ParticleModule;

public class ParticleModuleWrapper extends ModuleWrapper<ParticleModule> {

    @Override
    protected float reportPrefWidth() {
        return 150;
    }

    @Override
    protected void configureSlots() {
        addInputSlot("drawable",  ParticleModule.DRAWABLE);
        addInputSlot("offset",  ParticleModule.OFFSET);
        addInputSlot("position",  ParticleModule.POSITION);
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
}
