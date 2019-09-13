package com.rockbite.tools.talos.editor.wrappers;


import com.rockbite.tools.talos.runtime.modules.EmitterModule;

public class EmitterModuleWrapper extends ModuleWrapper<EmitterModule> {



    @Override
    protected float reportPrefWidth() {
        return 150;
    }


    @Override
    protected void configureSlots() {
        addInputSlot("name",4);
        addInputSlot("position", 3);
        addInputSlot("duration", 2);
        addInputSlot("loopable", 1);
        addInputSlot("emission", EmitterModule.RATE);
    }
}
