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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.ParticleEmitterDescriptor;
import com.talosvfx.talos.runtime.ScopePayload;
import com.talosvfx.talos.runtime.Slot;
import com.talosvfx.talos.runtime.assets.AssetProvider;
import com.talosvfx.talos.runtime.render.drawables.PolylineRenderer;
import com.talosvfx.talos.runtime.values.DrawableValue;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class PolylineModule extends AbstractModule {

    public static final int OFFSET = 0;
    public static final int THICKNESS = 1;
    public static final int COLOR = 2;
    public static final int TRANSPARENCY = 3;
    public static final int LEFT_TANGENT = 4;
    public static final int RIGHT_TANGENT = 5;

    public static final int OUTPUT = 0;

    NumericalValue offset;
    NumericalValue thickness;
    NumericalValue color;
    NumericalValue transparency;

    NumericalValue leftTangent;
    NumericalValue rightTangent;

    Color tmpColor = new Color();

    public String regionName;

    private DrawableValue outputValue;

    private PolylineRenderer polylineDrawable;
    public int pointCount = 2;

    @Override
    protected void defineSlots() {
        offset = createInputSlot(OFFSET);
        thickness = createInputSlot(THICKNESS);
        color = createInputSlot(COLOR);
        transparency = createInputSlot(TRANSPARENCY);

        leftTangent = createInputSlot(LEFT_TANGENT);
        rightTangent = createInputSlot(RIGHT_TANGENT);

        polylineDrawable = new PolylineRenderer();

        outputValue = (DrawableValue) createOutputSlot(OUTPUT, new DrawableValue());
        outputValue.setDrawable(polylineDrawable);
    }

    @Override
    public void fetchAllInputSlotValues() {
        int particleRequesterID = getScope().getRequesterID();

        polylineDrawable.setCurrentParticle(getScope().currParticle());

        for(int i = 0; i < pointCount; i++) {

            float pointAlpha = (float)i/(pointCount-1);
            getScope().set(ScopePayload.SUB_PARTICLE_ALPHA, pointAlpha);
            getScope().setCurrentRequesterID(getScope().newParticleRequester()); //Generate new cached id for update of particle

            for(Slot inputSlot : inputSlots.values()) {
                fetchInputSlotValue(inputSlot.getIndex());
            }

            float transparencyVal = 1f;
            if(!transparency.isEmpty()) {
                transparencyVal = transparency.getFloat();
            }

            if(color.isEmpty()) {
                tmpColor.set(Color.WHITE);
                tmpColor.a = transparencyVal;
            } else {
                tmpColor.set(color.get(0), color.get(1), color.get(2), transparencyVal);
            }

            float thicknessVal = 0.1f;
            if(!thickness.isEmpty()) {
                thicknessVal = thickness.getFloat();
            }

            if(offset.isEmpty()) {
                offset.set(0);
            }

            polylineDrawable.setPointData(i, offset.get(0), offset.get(1), thicknessVal, tmpColor);
            polylineDrawable.setTangents(leftTangent.get(0), leftTangent.get(1), rightTangent.get(0), rightTangent.get(1));
        }
        getScope().setCurrentRequesterID(particleRequesterID);
    }

    @Override
    public void processCustomValues () {
        outputValue.setDrawable(polylineDrawable);
    }

    public void setInterpolationPoints(int count) {
        pointCount = count + 2;
        polylineDrawable.setCount(count);
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("points", pointCount - 2);
        json.writeValue("regionName", regionName);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        pointCount = jsonData.getInt("points", 0) + 2;
        polylineDrawable.setCount(pointCount - 2);
        regionName = jsonData.getString("regionName", "fire");
    }


    public void setRegion (String regionName, TextureRegion region) {
        this.regionName = regionName;
        if(region != null) {
            polylineDrawable.setRegion(region);
        }
    }

    @Override
    public void setModuleGraph(ParticleEmitterDescriptor graph) {
        super.setModuleGraph(graph);
        final AssetProvider assetProvider = graph.getEffectDescriptor().getAssetProvider();
        setRegion(regionName, assetProvider.findAsset(regionName, TextureRegion.class));
    }
}
