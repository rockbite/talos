package com.rockbite.tools.talos.editor.wrappers;

import com.rockbite.tools.talos.runtime.modules.CartToRadModule;
import com.rockbite.tools.talos.runtime.modules.RadToCartModule;

public class RadToCartModuleWrapper extends ModuleWrapper<RadToCartModule> {

    @Override
    protected void configureSlots() {
        addInputSlot("angle: ", RadToCartModule.A);
        addInputSlot("velocity: ", RadToCartModule.L);

        addOutputSlot("XY", 0);
    }


    @Override
    protected float reportPrefWidth () {
        return 210;
    }
}
