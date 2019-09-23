package com.rockbite.tools.talos.editor.wrappers;


import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.runtime.Slot;
import com.rockbite.tools.talos.runtime.modules.*;

public class EmitterModuleWrapper extends ModuleWrapper<EmitterModule> {



    @Override
    protected float reportPrefWidth() {
        return 150;
    }


    @Override
    protected void configureSlots() {
        addInputSlot("duration", EmitterModule.DURATION);
        addInputSlot("emission", EmitterModule.RATE);
        addInputSlot("config", EmitterModule.CONFIG);
    }

    @Override
    public Class<? extends Module>  getSlotsPreferredModule(Slot slot) {
        if(slot.getIndex() == EmitterModule.RATE) {
            return StaticValueModule.class;
        }
        if(slot.getIndex() == EmitterModule.CONFIG) {
            return EmConfigModule.class;
        }

        return null;
    }

}
