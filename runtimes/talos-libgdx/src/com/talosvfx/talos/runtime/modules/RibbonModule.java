package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.ScopePayload;
import com.talosvfx.talos.runtime.Slot;
import com.talosvfx.talos.runtime.render.drawables.RibbonRenderer;
import com.talosvfx.talos.runtime.render.drawables.ShadedDrawable;
import com.talosvfx.talos.runtime.render.drawables.TextureRegionDrawable;
import com.talosvfx.talos.runtime.values.DrawableValue;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class RibbonModule extends AbstractModule {

    public static final int MAIN_REGION = 0;
    public static final int RIBBON_REGION = 1;
    public static final int THICKNESS = 2;
    public static final int TRANSPARENCY = 3;
    public static final int COLOR = 4;

    public static final int OUTPUT = 0;

    DrawableValue mainDrawableValue;
    DrawableValue ribbonDrawableValue;

    float memoryDuration = 1f;
    int detail;

    NumericalValue thicknessValue;
    NumericalValue transparencyValue;
    NumericalValue colorValue;

    DrawableValue outputValue;

    Color tmpColor = new Color();

    @Override
    protected void defineSlots() {
        mainDrawableValue = (DrawableValue) createInputSlot(MAIN_REGION, new DrawableValue());
        ribbonDrawableValue = (DrawableValue) createInputSlot(RIBBON_REGION, new DrawableValue());

        thicknessValue = createInputSlot(THICKNESS);
        transparencyValue = createInputSlot(TRANSPARENCY);
        colorValue = createInputSlot(COLOR);

        RibbonRenderer renderer = new RibbonRenderer();
        outputValue = (DrawableValue) createOutputSlot(OUTPUT, new DrawableValue());
        outputValue.setDrawable(renderer);
    }

    @Override
    public void fetchAllInputSlotValues() {
        super.fetchAllInputSlotValues();

        int cachedRequesterID = getScope().getRequesterID();

        RibbonRenderer renderer = (RibbonRenderer) outputValue.getDrawable();

        renderer.setCurrentParticle(getScope().currParticle());
        for(int i = 0; i < detail; i++) {

            float pointAlpha = (float)i/(detail-1);
            if(pointAlpha == 0) pointAlpha = 0.001f;
            getScope().set(ScopePayload.SUB_PARTICLE_ALPHA, pointAlpha);
            getScope().setCurrentRequesterID(getScope().newParticleRequester()); //New requester for sub particles

            for(Slot inputSlot : inputSlots.values()) {
                fetchInputSlotValue(inputSlot.getIndex());
            }

            float transparencyVal = 1f;
            if(!transparencyValue.isEmpty()) {
                transparencyVal = transparencyValue.getFloat();
            }

            if(colorValue.isEmpty()) {
                tmpColor.set(Color.WHITE);
                tmpColor.a = transparencyVal;
            } else {
                tmpColor.set(colorValue.get(0), colorValue.get(1), colorValue.get(2), transparencyVal);
            }

            float thicknessVal = 0.1f;
            if(!thicknessValue.isEmpty()) {
                thicknessVal = thicknessValue.getFloat();
            }

            renderer.setPointData(i, thicknessVal, tmpColor);
        }
        //renderer.adjustPointData();

        getScope().setCurrentRequesterID(cachedRequesterID);
    }

    @Override
    public void processCustomValues () {
        RibbonRenderer renderer = (RibbonRenderer) outputValue.getDrawable();

        TextureRegion mainRegion = null;
        TextureRegion ribbonRegion = null;

        if(!mainDrawableValue.isEmpty() && mainDrawableValue.getDrawable() != null) {
            mainRegion = mainDrawableValue.getDrawable().getTextureRegion();
        }
        if(!ribbonDrawableValue.isEmpty() && ribbonDrawableValue.getDrawable() != null) {

            if(ribbonDrawableValue.getDrawable() instanceof TextureRegionDrawable) {
                ribbonRegion = ribbonDrawableValue.getDrawable().getTextureRegion();
            } else if(ribbonDrawableValue.getDrawable() instanceof ShadedDrawable) {
                renderer.setShadedDrawable((ShadedDrawable) ribbonDrawableValue.getDrawable());
            }
        }

        renderer.setRegions(mainRegion, ribbonRegion);
    }

    public void setDetailCount(int count) {
        detail = count;
        RibbonRenderer renderer = (RibbonRenderer) outputValue.getDrawable();
        renderer.setConfig(detail, memoryDuration);
    }

    public void setMemoryDuration(float duration) {
        memoryDuration = duration;
        RibbonRenderer renderer = (RibbonRenderer) outputValue.getDrawable();
        renderer.setConfig(detail, memoryDuration);
    }

    public int getDetailCount() {
        return detail;
    }

    public float getMemoryDuration() {
        return memoryDuration;
    }


    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("details", detail);
        json.writeValue("memory", memoryDuration);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        detail = jsonData.getInt("details", 10);
        memoryDuration = jsonData.getFloat("memory", 10);
        RibbonRenderer renderer = (RibbonRenderer) outputValue.getDrawable();
        renderer.setConfig(detail, memoryDuration);
    }
}
