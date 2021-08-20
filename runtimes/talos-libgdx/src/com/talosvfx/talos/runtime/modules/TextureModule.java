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

package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.ParticleEmitterDescriptor;
import com.talosvfx.talos.runtime.assets.AssetProvider;
import com.talosvfx.talos.runtime.render.drawables.TextureRegionDrawable;
import com.talosvfx.talos.runtime.values.DrawableValue;

public class TextureModule extends AbstractModule {

    public static final int OUTPUT = 0;

    private DrawableValue userDrawable;
    private DrawableValue outputValue;

    public String regionName;

    @Override
    protected void defineSlots() {
        outputValue = (DrawableValue) createOutputSlot(OUTPUT, new DrawableValue());
        userDrawable = new DrawableValue();
        userDrawable.setEmpty(true);
    }

    @Override
    public void processCustomValues () {
        outputValue.set(userDrawable);
    }

    public void setRegion (String regionName, Sprite region) {
        this.regionName = regionName;
        
        if (region != null) {
            userDrawable.setDrawable(new TextureRegionDrawable(region));
        }
    }

    @Override
    public void setModuleGraph(ParticleEmitterDescriptor graph) {
        super.setModuleGraph(graph);
        final AssetProvider assetProvider = graph.getEffectDescriptor().getAssetProvider();
        setRegion(regionName, assetProvider.findAsset(regionName, Sprite.class));
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("regionName", regionName);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        regionName = jsonData.getString("regionName");
    }


}
