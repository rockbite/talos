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

        addInputSlot("point gen", ParticleModule.POINT_GENERATOR);
        addInputSlot("mesh gen", ParticleModule.MESH_GENERATOR);

        addSeparator(true);

        addInputSlot("life",  ParticleModule.LIFE);

        addSeparator(true);

        addInputSlot("color",  ParticleModule.COLOR);
        addInputSlot("transparency",  ParticleModule.TRANSPARENCY);

        addInputSlot("rotation",  ParticleModule.ROTATION);
        addInputSlot("position",  ParticleModule.POSITION);
        addInputSlot("pivot",  ParticleModule.PIVOT);

    }

    @Override
    public Class<? extends AbstractModule>  getSlotsPreferredModule(Slot slot) {

        if(slot.getIndex() == ParticleModule.POINT_GENERATOR) return SingleParticlePointDataGeneratorModule.class;
        if(slot.getIndex() == ParticleModule.MESH_GENERATOR) return QuadMeshGeneratorModule.class;
        if(slot.getIndex() == ParticleModule.LIFE) return StaticValueModule.class;
        if(slot.getIndex() == ParticleModule.ROTATION) return DynamicRangeModule.class;
        if(slot.getIndex() == ParticleModule.PIVOT) TalosMain.Instance().UIStage().getPreferred3DVectorClass();;
        if(slot.getIndex() == ParticleModule.COLOR) return GradientColorModule.class;
        if(slot.getIndex() == ParticleModule.TRANSPARENCY) return CurveModule.class;

        //Mode
        if(slot.getIndex() == ParticleModule.POSITION) return TalosMain.Instance().UIStage().getPreferred3DVectorClass();

        return null;
    }


}
