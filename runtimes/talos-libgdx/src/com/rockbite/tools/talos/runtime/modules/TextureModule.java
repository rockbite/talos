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

package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.runtime.ParticleEmitterDescriptor;
import com.rockbite.tools.talos.runtime.assets.AssetProvider;
import com.rockbite.tools.talos.runtime.render.drawables.TextureRegionDrawable;
import com.rockbite.tools.talos.runtime.values.DrawableValue;

public class TextureModule extends Module {

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
    public void processValues() {
        outputValue.set(userDrawable);
    }

    public void setRegion (String regionName, TextureRegion region) {
        this.regionName = regionName;
        if(region != null) {
            userDrawable.setDrawable(new TextureRegionDrawable(region));
        }
    }

    @Override
    public void setModuleGraph(ParticleEmitterDescriptor graph) {
        super.setModuleGraph(graph);
        final AssetProvider assetProvider = graph.getEffectDescriptor().getAssetProvider();
        setRegion(regionName, assetProvider.findRegion(regionName));
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
