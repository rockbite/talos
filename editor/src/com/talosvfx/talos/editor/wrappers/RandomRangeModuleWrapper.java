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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.widgets.FloatRangeInputWidget;
import com.talosvfx.talos.runtime.modules.RandomRangeModule;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class RandomRangeModuleWrapper extends ModuleWrapper<RandomRangeModule> {

    FloatRangeInputWidget inputRange;

    @Override
    public void attachModuleToMyOutput(ModuleWrapper moduleWrapper, int mySlot, int targetSlot) {
        super.attachModuleToMyOutput(moduleWrapper, mySlot, targetSlot);

        inputRange.setFlavour(module.getOutputValue().getFlavour());
    }

    @Override
    public void setSlotInactive(int slotTo, boolean isInput) {
        super.setSlotInactive(slotTo, isInput);
        if(!isInput) {
            inputRange.setFlavour(NumericalValue.Flavour.REGULAR);
            inputRange.setText("Min", "Max");
        }
    }

    @Override
    protected float reportPrefWidth() {
        return 250;
    }

    @Override
    protected void configureSlots() {

        addOutputSlot("result", 0);

        inputRange = new FloatRangeInputWidget("Min", "Max", getSkin());
        inputRange.setValue(1, 1);
        contentWrapper.add(inputRange).left().padTop(0).padLeft(4).expandX();

        leftWrapper.add(new Table()).expandY();
        rightWrapper.add(new Table()).expandY();

        inputRange.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateValues();
            }
        });

    }

    private void updateValues() {
        float min = inputRange.getMinValue();
        float max = inputRange.getMaxValue();

        module.setMinMax(min, max);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        setData(module.getMin(), module.getMax());
    }

    public void setData(float min, float max) {
        inputRange.setValue(min, max);
        updateValues();
    }
}
