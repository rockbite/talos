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

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.widgets.ui.common.AssetSelector;
import com.talosvfx.talos.runtime.vfx.modules.MaterialModule;
import com.talosvfx.talos.runtime.vfx.modules.SpriteMaterialModule;

public class SpriteMaterialModuleWrapper extends ModuleWrapper<SpriteMaterialModule> {

    private Label assetNameLabel;
    private AssetSelector<Texture> selector;

    private GameAsset<Texture> asset;

    public SpriteMaterialModuleWrapper() {
        super();
    }

    @Override
    public void setModuleToDefaults () {
        module.assetIdentifier = "white";
    }

    @Override
    protected float reportPrefWidth() {
        return 150;
    }

    @Override
    protected void configureSlots() {

        addOutputSlot("", MaterialModule.MATERIAL_MODULE);

        asset = AssetRepository.getInstance().getAssetForIdentifier("white", GameAssetType.SPRITE);

        selector = new AssetSelector<>("sprite", GameAssetType.SPRITE);
        selector.setValue(asset);
        contentWrapper.add(selector).growX().right().expandX();

        selector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                asset = selector.getValue();
                module.setAsset(asset.nameIdentifier);
            }
        });
    }


    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("asset", asset.nameIdentifier);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        String identifier = jsonData.getString("asset", "white");
        asset = AssetRepository.getInstance().getAssetForIdentifier(identifier, GameAssetType.SPRITE);
    }

}
