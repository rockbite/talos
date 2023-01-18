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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.widgets.TextureDropWidget;
import com.talosvfx.talos.editor.widgets.ui.common.LabelWithZoom;
import com.talosvfx.talos.runtime.modules.AbstractModule;
import com.talosvfx.talos.runtime.modules.TextureModule;

public class TextureModuleWrapper extends TextureDropModuleWrapper<TextureModule> {

    private Label assetNameLabel;

    public TextureModuleWrapper() {
        super();
    }

    @Override
    public void setModuleToDefaults () {
        module.regionName = "fire";
    }

    @Override
    protected float reportPrefWidth() {
        return 150;
    }

    @Override
    public void setModuleRegion (String name, Sprite region) {
        module.setRegion(name, region);
        assetNameLabel.setText(name);
    }

    @Override
    protected void configureSlots() {

        defaultRegion = new TextureRegion(new Texture(Gdx.files.internal("fire.png")));

        dropWidget = new TextureDropWidget<AbstractModule>(defaultRegion, getSkin());

        addOutputSlot("output", TextureModule.OUTPUT);

        assetNameLabel = new LabelWithZoom("fire", getSkin());

        contentWrapper.add(assetNameLabel).padLeft(10).colspan(2).expand().fill().row();

        contentWrapper.add(dropWidget).size(50).left().padLeft(10);
        contentWrapper.add().expandX();

    }


    @Override
    public void write (Json json) {
        super.write(json);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
    }

}
