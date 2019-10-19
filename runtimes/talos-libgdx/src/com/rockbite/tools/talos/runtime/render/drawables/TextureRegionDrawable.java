package com.rockbite.tools.talos.runtime.render.drawables;

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
    public void draw(Batch batch, float x, float y, float width, float height, float rotation) {
        batch.draw(region, x - width/2f, y - height/2f, width/2f, height/2f, width, height,1f,1f, rotation);
    }

    @Override
    public float getAspectRatio() {
        if(region == null) {
            return 1;
        }
        return region.getRegionWidth()/ (float)region.getRegionHeight();
    }

    @Override
    public void setSeed(float seed) {

    }
}
