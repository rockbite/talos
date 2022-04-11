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

import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.runtime.Slot;
import com.talosvfx.talos.runtime.modules.*;
import com.talosvfx.talos.runtime.modules.AbstractModule;

public class ParticleModuleWrapper extends ModuleWrapper<ParticleModule> {


    public ParticleModuleWrapper() {
        super();
    }

    @Override
    public void setModule(ParticleModule module) {
        super.setModule(module);
    }

    @Override
    protected float reportPrefWidth() {
        return 150;
    }

    @Override
    protected void configureSlots() {

        addSeparator(true);

        addInputSlot("life",  ParticleModule.LIFE);

        addSeparator(true);

        addInputSlot("start pos", ParticleModule.SPAWN_POSITION);
        addInputSlot("start rot", ParticleModule.SPAWN_ROTATION);
        addInputSlot("start velocity", ParticleModule.INITIAL_VELOCITY);
        addInputSlot("start rot velocity", ParticleModule.INITIAL_SPIN_VELOCITY);


        addSeparator(true);


        addInputSlot("velocity over time", ParticleModule.VELOCITY_OVER_TIME);
        addInputSlot("rot velocity over time", ParticleModule.SPIN_OVER_TIME);
        addInputSlot("gravity", ParticleModule.GRAVITY);
        addInputSlot("forces", ParticleModule.FORCES);
        addInputSlot("drag", ParticleModule.DRAG);

        addInputSlot("color",  ParticleModule.COLOR);
        addInputSlot("transparency",  ParticleModule.TRANSPARENCY);

        addInputSlot("pivot",  ParticleModule.PIVOT);
        addInputSlot("position override",  ParticleModule.POSITION_OVERRIDE);
        addInputSlot("rotation override",  ParticleModule.ROTATION_OVERRIDE);

    }

    @Override
    public Class<? extends AbstractModule>  getSlotsPreferredModule(Slot slot) {

        if(slot.getIndex() == ParticleModule.LIFE) return StaticValueModule.class;
        if(slot.getIndex() == ParticleModule.PIVOT) TalosMain.Instance().UIStage().getPreferred3DVectorClass();
        if(slot.getIndex() == ParticleModule.COLOR) return GradientColorModule.class;
        if(slot.getIndex() == ParticleModule.TRANSPARENCY) return CurveModule.class;

        //Mode
        if(slot.getIndex() == ParticleModule.SPAWN_POSITION) return TalosMain.Instance().UIStage().getPreferred3DVectorClass();
        if(slot.getIndex() == ParticleModule.SPAWN_ROTATION) return TalosMain.Instance().UIStage().getPreferred3DVectorClass();

        if(slot.getIndex() == ParticleModule.INITIAL_VELOCITY) return TalosMain.Instance().UIStage().getPreferred3DVectorClass();
        if(slot.getIndex() == ParticleModule.INITIAL_SPIN_VELOCITY) return TalosMain.Instance().UIStage().getPreferred3DVectorClass();
        if(slot.getIndex() == ParticleModule.VELOCITY_OVER_TIME) return TalosMain.Instance().UIStage().getPreferred3DVectorClass();
        if(slot.getIndex() == ParticleModule.SPIN_OVER_TIME) return TalosMain.Instance().UIStage().getPreferred3DVectorClass();
        if(slot.getIndex() == ParticleModule.DRAG) return TalosMain.Instance().UIStage().getPreferred3DVectorClass();
        if(slot.getIndex() == ParticleModule.GRAVITY) return TalosMain.Instance().UIStage().getPreferred3DVectorClass();
        if(slot.getIndex() == ParticleModule.FORCES) return TalosMain.Instance().UIStage().getPreferred3DVectorClass();

        if(slot.getIndex() == ParticleModule.POSITION_OVERRIDE) return TalosMain.Instance().UIStage().getPreferred3DVectorClass();
        if(slot.getIndex() == ParticleModule.ROTATION_OVERRIDE) return TalosMain.Instance().UIStage().getPreferred3DVectorClass();

        return null;
    }


}
