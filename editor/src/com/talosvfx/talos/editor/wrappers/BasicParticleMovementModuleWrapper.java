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

import com.talosvfx.talos.runtime.Slot;
import com.talosvfx.talos.runtime.modules.AbstractModule;
import com.talosvfx.talos.runtime.modules.BasicParticleMovementModule;
import com.talosvfx.talos.runtime.modules.CurveModule;
import com.talosvfx.talos.runtime.modules.DynamicRangeModule;
import com.talosvfx.talos.runtime.modules.GradientColorModule;
import com.talosvfx.talos.runtime.modules.ParticleModule;
import com.talosvfx.talos.runtime.modules.StaticValueModule;
import com.talosvfx.talos.runtime.modules.TextureModule;
import com.talosvfx.talos.runtime.modules.Vector2Module;

public class BasicParticleMovementModuleWrapper extends ModuleWrapper<BasicParticleMovementModule> {

    public BasicParticleMovementModuleWrapper () {
        super();
    }

    @Override
    public void setModule(BasicParticleMovementModule module) {
        super.setModule(module);
    }

    @Override
    protected float reportPrefWidth() {
        return 150;
    }

    @Override
    protected void configureSlots() {
        addInputSlot("offset",  BasicParticleMovementModule.OFFSET);
        addInputSlot("velocity",  BasicParticleMovementModule.VELOCITY);
        addInputSlot("target",  BasicParticleMovementModule.TARGET);
        addInputSlot("angle",  BasicParticleMovementModule.ANGLE);


        addOutputSlot("position", BasicParticleMovementModule.POSITION);

    }

    @Override
    public Class<? extends AbstractModule>  getSlotsPreferredModule(Slot slot) {

        if(slot.getIndex() == BasicParticleMovementModule.OFFSET) return Vector2Module.class;
        if(slot.getIndex() == BasicParticleMovementModule.TARGET) return Vector2Module.class;
        if(slot.getIndex() == BasicParticleMovementModule.VELOCITY) return DynamicRangeModule.class;
        if(slot.getIndex() == BasicParticleMovementModule.ANGLE) return DynamicRangeModule.class;
        if(slot.getIndex() == BasicParticleMovementModule.POSITION) return Vector2Module.class;

        return null;
    }


}
