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

package com.talosvfx.talos.runtime;

import com.talosvfx.talos.runtime.modules.AbstractModule;
import com.talosvfx.talos.runtime.values.Value;

public class Slot {

    private int index;
    private boolean isInput;
    private Flavour flavour;

    private AbstractModule currentModule;
    private AbstractModule targetModule;
    private Slot targetSlot;

    private Value value;

    private String text = "";

    public AbstractModule getTargetModule() {
        return targetModule;
    }

    public boolean isInput() {
        return isInput;
    }

    public String getText() {
        return text;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    enum Flavour {
        ANGLE
    }

    public Slot(AbstractModule currentModule, int index, boolean isInput) {
        this.currentModule = currentModule;
        this.index = index;
        this.isInput = isInput;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public void connect(AbstractModule targetModule, Slot targetSlot) {
        this.targetModule = targetModule;
        this.targetSlot = targetSlot;
        if(value != null) {
            value.setEmpty(true);
        }
    }

    public Slot getTargetSlot() {
        return targetSlot;
    }

    public Value getValue() {
        return value;
    }

    public boolean isCompatable(Slot slot) {
        if(value == null || slot.value == null) return true;

        return value.getClass() == slot.value.getClass();
    }

    public void detach() {
        this.targetModule = null;
        this.targetSlot = null;
    }

}
