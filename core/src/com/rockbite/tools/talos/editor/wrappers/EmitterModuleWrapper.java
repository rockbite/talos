package com.rockbite.tools.talos.editor.wrappers;


import com.badlogic.gdx.utils.JsonValue;
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
        addInputSlot("delay", EmitterModule.DELAY);
        addInputSlot("duration", EmitterModule.DURATION);
        addInputSlot("emission", EmitterModule.RATE);
    }

    @Override
    public Class<? extends Module>  getSlotsPreferredModule(Slot slot) {
        if(slot.getIndex() == EmitterModule.RATE) {
            return StaticValueModule.class;
        }

        return null;
    }

    @Override
    public void write(JsonValue value) {

    }

    @Override
    public void read(JsonValue value) {

    }
}
