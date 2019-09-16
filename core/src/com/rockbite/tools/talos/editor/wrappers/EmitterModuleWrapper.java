package com.rockbite.tools.talos.editor.wrappers;


import com.rockbite.tools.talos.runtime.Slot;
import com.rockbite.tools.talos.runtime.modules.DynamicRangeModule;
import com.rockbite.tools.talos.runtime.modules.EmitterModule;
import com.rockbite.tools.talos.runtime.modules.Module;
import com.rockbite.tools.talos.runtime.modules.StaticValueModule;

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

    @Override
    public Class<? extends Module>  getSlotsPreferredModule(Slot slot) {
        if(slot.getIndex() == EmitterModule.RATE) {
            return StaticValueModule.class;
        }

        return null;
    }
}
