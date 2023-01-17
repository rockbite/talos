package com.talosvfx.talos.editor.wrappers;

import com.talosvfx.talos.runtime.vfx.modules.CartToRadModule;

public class CartToRadModuleWrapper extends ModuleWrapper<CartToRadModule> {

    @Override
    protected void configureSlots() {
        addInputSlot("position: ", CartToRadModule.POSITION);

        addOutputSlot("Angle", CartToRadModule.ANGLE);
        addOutputSlot("Length", CartToRadModule.LENGTH);
    }

    @Override
    protected float reportPrefWidth () {
        return 210;
    }
}
