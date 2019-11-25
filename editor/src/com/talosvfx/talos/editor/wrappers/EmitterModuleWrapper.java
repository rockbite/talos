/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.talosvfx.talos.editor.wrappers;


import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.talosvfx.talos.runtime.Slot;
import com.talosvfx.talos.runtime.modules.*;

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
    public Class<? extends AbstractModule>  getSlotsPreferredModule(Slot slot) {
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
