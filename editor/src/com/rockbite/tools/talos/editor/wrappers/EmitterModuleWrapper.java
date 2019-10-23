package com.rockbite.tools.talos.editor.wrappers;


import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.rockbite.tools.talos.runtime.Slot;
import com.rockbite.tools.talos.runtime.modules.*;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class EmitterModuleWrapper extends ModuleWrapper<EmitterModule> {

    VisTextField delayField;
    VisTextField durationField;
    VisTextField emissionField;

    @Override
    protected float reportPrefWidth() {
        return 180;
    }


    @Override
    protected void configureSlots() {
        delayField = addInputSlotWithTextField("delay: ", EmitterModule.DELAY, 60, true);
        durationField = addInputSlotWithTextField("duration: ", EmitterModule.DURATION, 60, true);
        emissionField = addInputSlotWithTextField("emission: ", EmitterModule.RATE, 60, true);

        delayField.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                 module.defaultDelay = floatFromText(delayField);
            }
        });

        durationField.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                module.defaultDuration = floatFromText(durationField);
            }
        });

        emissionField.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                module.defaultRate = floatFromText(emissionField);
            }
        });

        addInputSlot("config", EmitterModule.CONFIG).pad(3);
    }

    @Override
    public Class<? extends Module>  getSlotsPreferredModule(Slot slot) {
        if(slot.getIndex() == EmitterModule.RATE) {
            return StaticValueModule.class;
        }
        if(slot.getIndex() == EmitterModule.CONFIG) {
            return EmConfigModule.class;
        }
        if(slot.getIndex() == EmitterModule.DURATION) {
            return StaticValueModule.class;
        }
        if(slot.getIndex() == EmitterModule.DELAY) {
            return StaticValueModule.class;
        }

        return null;
    }

    @Override
    public void setModule(EmitterModule module) {
        super.setModule(module);
        delayField.setText(module.defaultDelay + "");
        durationField.setText(module.defaultDuration + "");
        emissionField.setText(module.defaultRate + "");
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        delayField.setText(module.defaultDelay + "");
        durationField.setText(module.defaultDuration + "");
        emissionField.setText(module.defaultRate + "");
    }

}
