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
import com.talosvfx.talos.runtime.modules.ColorModule;
import com.talosvfx.talos.runtime.modules.StaticValueModule;
import com.talosvfx.talos.runtime.modules.StripMeshGeneratorModule;
import com.talosvfx.talos.runtime.modules.Vector2Module;

public class StripMeshGeneratorModuleWrapper extends ModuleWrapper<StripMeshGeneratorModule> {


    public StripMeshGeneratorModuleWrapper () {
        super();
    }

    @Override
    public void setModule(StripMeshGeneratorModule module) {
        super.setModule(module);
    }

    @Override
    protected float reportPrefWidth() {
        return 150;
    }

    @Override
    protected void configureSlots() {
        addInputSlot("uvs", StripMeshGeneratorModule.UVS);
        addInputSlot("offset", StripMeshGeneratorModule.OFFSET);
        addInputSlot("colour", StripMeshGeneratorModule.COLOUR);
        addInputSlot("thickness", StripMeshGeneratorModule.THICKNESS);
        addInputSlot("transparency", StripMeshGeneratorModule.TRANSPARENCY);

        addOutputSlot("strip", StripMeshGeneratorModule.MODULE);
    }

    @Override
    public Class<? extends AbstractModule> getSlotsPreferredModule (Slot slot) {
        if (slot.getIndex() == StripMeshGeneratorModule.UVS) return Vector2Module.class;
        if (slot.getIndex() == StripMeshGeneratorModule.OFFSET) return Vector2Module.class;
        if (slot.getIndex() == StripMeshGeneratorModule.COLOUR) return ColorModule.class;
        if (slot.getIndex() == StripMeshGeneratorModule.TRANSPARENCY) return StaticValueModule.class;
        if (slot.getIndex() == StripMeshGeneratorModule.THICKNESS) return StaticValueModule.class;

        return null;
    }


}
