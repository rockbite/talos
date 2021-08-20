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

package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.runtime.ScopePayload;
import com.talosvfx.talos.runtime.Slot;
import com.talosvfx.talos.runtime.values.Value;

import java.util.Random;

public class RandomInputModule extends AbstractModule {

    Class valueType = null;

    private Random random = new Random();

    public int slotCount = 0;

    @Override
    protected void defineSlots() {
        addInputSlot(0);
        createOutputSlot(0, null);
    }

    public void addInputSlot(int key) {
        Slot slot = new Slot(this, key, true);
        inputSlots.put(key, slot);
    }

    @Override
    public void attachModuleToMyInput(AbstractModule module, int mySlot, int targetSlot) {
        addInputSlot(slotCount++);
        super.attachModuleToMyInput(module, mySlot, targetSlot);

        // let's figure out the type
        if(valueType == null) {
            valueType = module.getOutputSlot(targetSlot).getValue().getClass();
        } else {
            Class newValueType = module.getOutputSlot(targetSlot).getValue().getClass();
            if(valueType != newValueType) {
                // changing value detaching all previous values
                // detach code goes here
                valueType = newValueType;
            }
        }
        // re init all previous values
        try {
            for(Slot slot : getInputSlots().values()) {
                slot.setValue((Value) ClassReflection.newInstance(valueType));
            }
            getOutputSlot(0).setValue((Value) ClassReflection.newInstance(valueType));
        } catch (ReflectionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processCustomValues () {

        Value output = outputSlots.get(0).getValue();
        if(output != null) {
            random.setSeed((long) ((getScope().getFloat(ScopePayload.EMITTER_ALPHA_AT_P_INIT) * 10000 * (index+1) * 1000)));
            int index = MathUtils.round(random.nextFloat() * (inputSlots.size - 1));

            Value input = inputSlots.get(index).getValue();
            if(input != null && !input.isEmpty()) {
                output.set(input);
            }
        }
    }
}
