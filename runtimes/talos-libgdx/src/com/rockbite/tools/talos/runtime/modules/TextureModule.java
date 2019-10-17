package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.runtime.ParticleEmitterDescriptor;
import com.rockbite.tools.talos.runtime.assets.AssetProvider;
import com.rockbite.tools.talos.runtime.render.TextureRegionDrawable;
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
