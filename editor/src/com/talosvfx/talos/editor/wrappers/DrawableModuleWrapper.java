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
import com.talosvfx.talos.runtime.modules.CurveModule;
import com.talosvfx.talos.runtime.modules.DrawableModule;
import com.talosvfx.talos.runtime.modules.DynamicRangeModule;
import com.talosvfx.talos.runtime.modules.GradientColorModule;
import com.talosvfx.talos.runtime.modules.MaterialModule;
import com.talosvfx.talos.runtime.modules.ParticleModule;
import com.talosvfx.talos.runtime.modules.QuadMeshGeneratorModule;
import com.talosvfx.talos.runtime.modules.SingleParticlePointDataGeneratorModule;
import com.talosvfx.talos.runtime.modules.SpriteMaterialModule;
import com.talosvfx.talos.runtime.modules.StaticValueModule;
import com.talosvfx.talos.runtime.modules.Vector2Module;

public class DrawableModuleWrapper extends ModuleWrapper<DrawableModule> {


    public DrawableModuleWrapper () {
        super();
    }

    @Override
    public void setModule(DrawableModule module) {
        super.setModule(module);
    }

    @Override
    protected float reportPrefWidth() {
        return 150;
    }

    @Override
    protected void configureSlots() {
        addInputSlot("material", MaterialModule.MATERIAL_MODULE);

        addInputSlot("point", DrawableModule.POINT_GENERATOR);
        addInputSlot("mesh", DrawableModule.MESH_GENERATOR);

    }

    @Override
    public Class<? extends AbstractModule>  getSlotsPreferredModule(Slot slot) {

        if(slot.getIndex() == DrawableModule.MATERIAL_IN) return SpriteMaterialModule.class;
        if(slot.getIndex() == DrawableModule.POINT_GENERATOR) return SingleParticlePointDataGeneratorModule.class;
        if(slot.getIndex() == DrawableModule.MESH_GENERATOR) return QuadMeshGeneratorModule.class;

        return null;
    }


}
