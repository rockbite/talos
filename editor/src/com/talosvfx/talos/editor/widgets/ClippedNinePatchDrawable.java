package com.talosvfx.talos.editor.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

/**
 * @author Eduard Peshtmaljyan
 */
public class ClippedNinePatchDrawable extends BaseDrawable {

    private ClippedNinePatch patch;

    public float maskScaleX = 1, maskScaleY = 1;

    public ClippedNinePatchDrawable (ClippedNinePatchDrawable drawable) {
        super(drawable);
        this.patch = drawable.patch;
    }

    public ClippedNinePatchDrawable (ClippedNinePatch patch) {
        this.patch = patch;
    }

    public ClippedNinePatchDrawable (TextureAtlas.AtlasRegion region) {
        this.patch = new ClippedNinePatch(region,
                region.splits[0],
                region.splits[1],
                region.splits[2],
                region.splits[3]);
    }

    public void setMaskScale (float clipScaleX, float clipScaleY) {
        this.maskScaleX = clipScaleX;
        this.maskScaleY = clipScaleY;
    }

    public void setColor (Color color) {
        patch.setColor(color);
    }

    @Override
    public void draw (Batch batch, float x, float y, float width, float height) {
        patch.setMaskScale(maskScaleX, maskScaleY);
        patch.draw(batch, x, y, width, height);
    }
}
