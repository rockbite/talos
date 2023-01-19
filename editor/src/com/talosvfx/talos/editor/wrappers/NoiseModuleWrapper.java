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
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.widgets.NoiseImage;
import com.talosvfx.talos.runtime.vfx.Slot;

import com.talosvfx.talos.runtime.vfx.modules.AbstractModule;
import com.talosvfx.talos.runtime.vfx.modules.InputModule;
import com.talosvfx.talos.runtime.vfx.modules.NoiseModule;

public class NoiseModuleWrapper extends ModuleWrapper<NoiseModule> {

    NoiseImage noiseImage;
    Slider slider;

    @Override
    protected float reportPrefWidth() {
        return 165;
    }


    @Override
    protected void configureSlots() {

        addInputSlot("X: (0 to 1)", NoiseModule.X);
        addInputSlot("Y: (0 to 1)", NoiseModule.Y);

        addOutputSlot("output", NoiseModule.OUTPUT);

        slider = new Slider(0.5f, 20f, 0.1f, false, getSkin());
        leftWrapper.add(slider).growX().padRight(2f).padBottom(5f).row();
        slider.setValue(0.5f);

        noiseImage = new NoiseImage(getSkin());
        leftWrapper.add(noiseImage).expandX().fillX().growX().height(100).padRight(3).padBottom(3);

        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float frequency = 20f - slider.getValue() + 0.5f;
                noiseImage.setFrequency(frequency);
                module.setFrequency(frequency);
            }
        });

        rightWrapper.add().expandY();
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        slider.setValue(20f - module.getFrequency() + 0.5f);

    }


    @Override
    public Class<? extends AbstractModule>  getSlotsPreferredModule(Slot slot) {

        if(slot.getIndex() == NoiseModule.X) return InputModule.class;
        if(slot.getIndex() == NoiseModule.Y) return InputModule.class;

        return null;
    }

}
