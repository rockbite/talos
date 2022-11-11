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

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.ParticleEmitterDescriptor;
import com.talosvfx.talos.runtime.ScopePayload;
import com.talosvfx.talos.runtime.assets.AssetProvider;
import com.talosvfx.talos.runtime.render.drawables.SpriteAnimationDrawable;
import com.talosvfx.talos.runtime.values.DrawableValue;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class FlipbookModule extends AbstractModule {

    public static final int PHASE = 0;

    public static final int OUTPUT = 0;

    public String regionName;
    private DrawableValue userDrawable;
    private DrawableValue outputValue;

    NumericalValue phaseVal;

    int rows = 1;
    int cols = 1;

    public float duration;

    SpriteAnimationDrawable spriteAnimation;

    @Override
    protected void defineSlots() {
        phaseVal = createInputSlot(PHASE);
        spriteAnimation = new SpriteAnimationDrawable();

        outputValue = (DrawableValue) createOutputSlot(OUTPUT, new DrawableValue());
        userDrawable = new DrawableValue();
        userDrawable.setEmpty(true);
    }

    @Override
    public void processCustomValues () {
        if(phaseVal.isEmpty()) {
            phaseVal.set(getScope().getFloat(ScopePayload.TOTAL_TIME));
        }

        float time = phaseVal.getFloat();
        time = time / duration;

        spriteAnimation.setPhase(time - (int)time); // maybe another approach is better.

        outputValue.set(userDrawable);
    }

    public void setRegion (String regionName, TextureRegion region) {
        this.regionName = regionName;
        if(region != null) {
            spriteAnimation.set(region, rows, cols);
            userDrawable.setDrawable(spriteAnimation);
        }
    }


    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("regionName", regionName);

        json.writeValue("rows", rows);
        json.writeValue("cols", cols);

        json.writeValue("duration", duration);
    }


    @Override
    public void setModuleGraph(ParticleEmitterDescriptor graph) {
        super.setModuleGraph(graph);
        final AssetProvider assetProvider = graph.getEffectDescriptor().getAssetProvider();
        setRegion(regionName, assetProvider.findAsset(regionName, TextureRegion.class));
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        regionName = jsonData.getString("regionName", "fire");

        rows = jsonData.getInt("rows", 1);
        cols = jsonData.getInt("cols", 1);
        duration = jsonData.getFloat("duration", 1);
    }

    public void setRows(int value) {
        rows = value;
        if(rows < 1) rows = 1;
        spriteAnimation.set(rows, cols);
    }

    public void setCols(int value) {
        cols = value;
        if(cols < 1) cols = 1;
        spriteAnimation.set(rows, cols);
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }
}
