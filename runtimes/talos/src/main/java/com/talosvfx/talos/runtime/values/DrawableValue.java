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

import com.talosvfx.talos.runtime.ParticleDrawable;

public class DrawableValue extends Value {

    ParticleDrawable drawable;

    public DrawableValue() {
        setEmpty(true);
    }

    @Override
    public void set(Value value) {
        if(value.isEmpty()) {
            setEmpty(true);
            drawable = null;
            return;
        }
        drawable = ((DrawableValue)value).getDrawable();

        setEmpty(drawable == null);
    }

    public ParticleDrawable getDrawable() {
        return drawable;
    }

    public void setDrawable(ParticleDrawable drawable) {
        this.drawable = drawable;
        setEmpty(drawable == null);
    }
}
