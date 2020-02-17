package com.talosvfx.talos.editor.wrappers;

import com.talosvfx.talos.runtime.modules.ShaderModule;

public class ShaderModuleWrapper extends ModuleWrapper<ShaderModule> {

    @Override
    protected void configureSlots() {
        addInputSlot("color", ShaderModule.COLOR);

        addOutputSlot("output", ShaderModule.OUTPUT);
    }

    @Override
    protected float reportPrefWidth () {
        return 170;
    }
}
