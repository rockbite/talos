package com.rockbite.tools.talos.runtime.render;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.rockbite.tools.talos.runtime.ParticleDrawable;

public class TextureRegionDrawable implements ParticleDrawable {

    private TextureRegion region;

    public TextureRegionDrawable(TextureRegion region) {
        this.region = region;
    }

    public void setRegion(TextureRegion region) {
        this.region = region;
    }

    @Override
    public void draw(Batch batch, float x, float y, float originX, float originY, float width, float height, float rotation) {
        batch.draw(region, x, y, originX, originY, width, height,1f,1f, rotation);
    }
}
