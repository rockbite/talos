package com.talosvfx.talos.editor.wrappers;

import com.talosvfx.talos.runtime.modules.Sample2DFragModule;

public class Sample2DFragModuleWrapper extends ModuleWrapper<Sample2DFragModule> {

    @Override
    protected void configureSlots() {
        addInputSlot("texture", Sample2DFragModule.TEXTURE);
        addInputSlot("position", Sample2DFragModule.POSITION);

        addOutputSlot("rgba", Sample2DFragModule.RGBA);
        addOutputSlot("r", Sample2DFragModule.R);
        addOutputSlot("g", Sample2DFragModule.G);
        addOutputSlot("b", Sample2DFragModule.B);
        addOutputSlot("a", Sample2DFragModule.A);
    }

    @Override
    protected float reportPrefWidth () {
        return 170;
    }
}
