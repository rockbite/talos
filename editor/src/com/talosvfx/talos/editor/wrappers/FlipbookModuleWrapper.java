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

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.widgets.FloatInputWidget;
import com.talosvfx.talos.editor.widgets.IntegerInputWidget;
import com.talosvfx.talos.editor.widgets.TextureDropWidget;
import com.talosvfx.talos.runtime.vfx.modules.FlipbookModule;
import com.talosvfx.talos.runtime.vfx.modules.AbstractModule;

public class FlipbookModuleWrapper extends TextureDropModuleWrapper<FlipbookModule> {

    IntegerInputWidget rows;
    IntegerInputWidget cols;

    FloatInputWidget duration;

    @Override
    public void setModuleToDefaults () {
        module.regionName = "fire";
    }

    @Override
    protected void configureSlots() {
        super.configureSlots();
        dropWidget = new TextureDropWidget<AbstractModule>(defaultRegion, getSkin(), 100f);

        addInputSlot("phase",  FlipbookModule.PHASE);

        rows =  new IntegerInputWidget("Rows", getSkin());
        cols =  new IntegerInputWidget("Cols", getSkin());
        duration =  new FloatInputWidget("Duration", getSkin());
        rows.setValue(1);
        cols.setValue(1);
        duration.setValue(1f);

        leftWrapper.add(rows).padTop(5f).left().expandX().row();
        leftWrapper.add(cols).left().expandX().row();
        leftWrapper.add(duration).padLeft(5).left().expandX().row();

        rightWrapper.add(dropWidget).size(100).right().row();

        rightWrapper.add().growY().row();
        addOutputSlot("output", FlipbookModule.OUTPUT);

        rows.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                module.setRows(rows.getValue());
            }
        });


        duration.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                module.duration = duration.getValue();
            }
        });

        cols.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                module.setCols(cols.getValue());
            }
        });
    }

    @Override
    public void setModuleRegion(String name, Sprite region) {
        module.setRegion(name, region);
    }

    @Override
    protected float reportPrefWidth() {
        return 200;
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        rows.setValue(module.getRows());
        cols.setValue(module.getCols());
        duration.setValue(module.duration);
    }
}
