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

package com.talosvfx.talos.runtime.values;

public class EmConfigValue extends Value {

    public boolean attached = false;
    public boolean continuous = true;
    public boolean aligned = false;
    public boolean additive = true;
    public boolean isBlendAdd = false;
    public boolean immortal = false;

    @Override
    public void set(Value value) {
        set((EmConfigValue) value);
    }

    public void set(EmConfigValue from) {
        this.attached = from.attached;
        this.continuous = from.continuous;
        this.aligned = from.aligned;
        this.additive = from.additive;
        this.isBlendAdd = from.isBlendAdd;
        this.immortal = from.immortal;
    }
}
