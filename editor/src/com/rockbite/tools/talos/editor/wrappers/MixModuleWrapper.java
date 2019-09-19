package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.runtime.modules.MixModule;

public class MixModuleWrapper extends ModuleWrapper<MixModule> {

    @Override
    protected float reportPrefWidth() {
        return 180;
    }

    @Override
    public void write(JsonValue value) {

    }

    @Override
    public void read(JsonValue value) {

    }


    @Override
    protected void configureSlots() {

        addInputSlot("Value One", MixModule.VAL1);
        addInputSlot("mix ratio (0..1)", MixModule.ALPHA);
        addInputSlot("Value Two", MixModule.VAL2);

        addOutputSlot("result", 0);
    }
}
