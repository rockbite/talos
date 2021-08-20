package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.render.drawables.NinePatchDrawable;
import com.talosvfx.talos.runtime.values.DrawableValue;

public class NinePatchModule extends AbstractModule {

    public static final int INPUT = 0;
    public static final int OUTPUT = 0;

    private DrawableValue inputDrawable;
    private DrawableValue outputValue;

    private int[] splits = new int[4];

    @Override
    protected void defineSlots () {
        inputDrawable = (DrawableValue) createInputSlot(INPUT, new DrawableValue());

        NinePatchDrawable patchDrawable = new NinePatchDrawable();
        outputValue = (DrawableValue) createOutputSlot(OUTPUT, new DrawableValue());
        outputValue.setDrawable(patchDrawable);
    }

    @Override
    public void processCustomValues () {
        NinePatchDrawable patchDrawable = (NinePatchDrawable) outputValue.getDrawable();

        TextureRegion region = null;

        if(!inputDrawable.isEmpty() && inputDrawable.getDrawable() != null) {
            region = inputDrawable.getDrawable().getTextureRegion();
        }

        patchDrawable.setRegion(region, getSplits());
    }


    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("lsplit", splits[0]);
        json.writeValue("rsplit", splits[1]);
        json.writeValue("tsplit", splits[2]);
        json.writeValue("bsplit", splits[3]);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        splits[0] = jsonData.getInt("lsplit", 0);
        splits[1] = jsonData.getInt("rsplit", 0);
        splits[2] = jsonData.getInt("tsplit", 0);
        splits[3] = jsonData.getInt("bsplit", 0);
    }

    public void setSplits (int left, int right, int top, int bottom) {
        splits[0] = left;
        splits[1] = right;
        splits[2] = top;
        splits[3] = bottom;
    }

    public int[] getSplits () {
        return splits;
    }

    public void resetPatch () {
        NinePatchDrawable patchDrawable = (NinePatchDrawable) outputValue.getDrawable();
        patchDrawable.resetPatch(splits);
    }
}
