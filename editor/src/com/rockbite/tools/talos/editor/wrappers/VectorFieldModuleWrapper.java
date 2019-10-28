package com.rockbite.tools.talos.editor.wrappers;

import com.rockbite.tools.talos.runtime.modules.VectorFieldModule;

public class VectorFieldModuleWrapper extends ModuleWrapper<VectorFieldModule> {


    @Override
    protected void configureSlots () {
        addInputSlot("field position", VectorFieldModule.POSITION);
        addInputSlot("field size", VectorFieldModule.SIZE_SCALE);
        addInputSlot("field force", VectorFieldModule.FORCE_SCALE);

        addOutputSlot("velocity", VectorFieldModule.VELOCITY);
        addOutputSlot("angle", VectorFieldModule.ANGLE);
    }


    @Override
    protected float reportPrefWidth () {
        return 180;
    }
}
