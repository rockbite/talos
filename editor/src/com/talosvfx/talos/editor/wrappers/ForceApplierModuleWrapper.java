package com.talosvfx.talos.editor.wrappers;

import com.talosvfx.talos.runtime.vfx.modules.ForceApplierModule;

public class ForceApplierModuleWrapper extends ModuleWrapper<ForceApplierModule> {


    @Override
    protected void configureSlots() {
        addInputSlot("sum forces: ", ForceApplierModule.SUM_FORCES);

        addOutputSlot("angle: ", ForceApplierModule.ANGLE);
        addOutputSlot("velocity", ForceApplierModule.VELOCITY);
    }


    @Override
    protected float reportPrefWidth () {
        return 210;
    }
}
