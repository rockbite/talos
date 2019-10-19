package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.graphics.g2d.Batch;

public interface ParticleDrawable {

    void draw(Batch batch, float x, float y, float width, float height, float rotation);

    float getAspectRatio();

    void setSeed(float seed);
}
