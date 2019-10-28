package com.rockbite.tools.talos.editor.wrappers;

import com.rockbite.tools.talos.runtime.modules.CartToRadModule;

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
