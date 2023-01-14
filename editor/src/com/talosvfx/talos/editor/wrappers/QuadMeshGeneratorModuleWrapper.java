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
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.talosvfx.talos.runtime.Slot;
import com.talosvfx.talos.runtime.modules.AbstractModule;
import com.talosvfx.talos.runtime.modules.QuadMeshGeneratorModule;
import com.talosvfx.talos.runtime.modules.Vector2Module;

public class QuadMeshGeneratorModuleWrapper extends ModuleWrapper<QuadMeshGeneratorModule> {

    private VisCheckBox billboard;

    public QuadMeshGeneratorModuleWrapper () {
        super();
    }

    @Override
    public void setModule(QuadMeshGeneratorModule module) {
        super.setModule(module);
    }

    @Override
    protected float reportPrefWidth() {
        return 150;
    }

    @Override
    protected void configureSlots() {
        addInputSlot("size", QuadMeshGeneratorModule.SIZE);
        addOutputSlot("quad", QuadMeshGeneratorModule.MODULE);

        billboard = new VisCheckBox("billboard");
        billboard.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                module.setBillboard(billboard.isChecked());
            }
        });

        leftWrapper.add(billboard).left().expandX().padLeft(3).row();


    }

    @Override
    public Class<? extends AbstractModule> getSlotsPreferredModule (Slot slot) {
        if (slot.getIndex() == QuadMeshGeneratorModule.SIZE) return Vector2Module.class;

        return null;
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);

    }

    @Override
    public void write (Json json) {
        super.write(json);
    }
}
