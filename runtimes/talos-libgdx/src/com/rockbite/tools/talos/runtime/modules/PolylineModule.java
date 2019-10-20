package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.runtime.ParticleEmitterDescriptor;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.Slot;
import com.rockbite.tools.talos.runtime.assets.AssetProvider;
import com.rockbite.tools.talos.runtime.render.drawables.PolylineDrawable;
import com.rockbite.tools.talos.runtime.render.drawables.TextureRegionDrawable;
import com.rockbite.tools.talos.runtime.values.DrawableValue;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class PolylineModule extends Module {

    public static final int OFFSET = 0;
    public static final int THICKNESS = 1;
    public static final int COLOR = 2;
    public static final int TRANSPARENCY = 3;

    public static final int OUTPUT = 0;

    NumericalValue offset;
    NumericalValue thickness;
    NumericalValue color;
    NumericalValue transparency;

    Color tmpColor = new Color();

    public String regionName;

    private DrawableValue outputValue;

    private PolylineDrawable polylineDrawable;
    public int pointCount = 2;

    @Override
    protected void defineSlots() {
        offset = createInputSlot(OFFSET);
        thickness = createInputSlot(THICKNESS);
        color = createInputSlot(COLOR);
        transparency = createInputSlot(TRANSPARENCY);

        polylineDrawable = new PolylineDrawable();

        outputValue = (DrawableValue) createOutputSlot(OUTPUT, new DrawableValue());
        outputValue.setDrawable(polylineDrawable);
    }

    @Override
    public void fetchAllInputSlotValues() {
        float requester = getScope().get(ScopePayload.REQUESTER_ID).getFloat();
        polylineDrawable.setSeed(requester);

        for(int i = 0; i < pointCount; i++) {

            float pointAlpha = (float)i/(pointCount-1);
            getScope().set(ScopePayload.SECONDARY_SEED, pointAlpha);
            getScope().set(ScopePayload.REQUESTER_ID, requester + pointAlpha*0.1f);

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

            polylineDrawable.setPointData(i, 0, offset.getFloat(), thicknessVal, tmpColor);
        }
        getScope().set(ScopePayload.REQUESTER_ID, requester);
    }

    @Override
    public void processValues() {
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
        setRegion(regionName, assetProvider.findRegion(regionName));
    }
}
