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
import com.kotcrab.vis.ui.widget.VisTextArea;
import com.talosvfx.talos.runtime.vfx.modules.ScriptModule;

public class ScriptModuleWrapper extends ModuleWrapper<ScriptModule> {

    private VisTextArea script;

    @Override
    protected void configureSlots() {

    	addInputSlot("i1", 0);
    	addInputSlot("i2", 1);
    	addInputSlot("i3", 2);
    	addInputSlot("i4", 3);
    	addInputSlot("i5", 4);

		addOutputSlot("o1", 0);
		addOutputSlot("o2", 1);
		addOutputSlot("o3", 2);
		addOutputSlot("o4", 3);
		addOutputSlot("o5", 4);

        script = new VisTextArea();
        contentWrapper.add(script).width(220).height(100);


        script.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                module.setScript(script.getText());
            }
        });
    }

    @Override
    protected float reportPrefWidth() {
        return 320;
    }

	@Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);
		if (module.getScript() != null) {
			script.setText(module.getScript());
			module.setScript(module.getScript());
		}
	}


}
