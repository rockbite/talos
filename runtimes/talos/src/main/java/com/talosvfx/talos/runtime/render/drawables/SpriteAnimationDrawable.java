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

package com.talosvfx.talos.runtime.render.drawables;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticleDrawable;

public class SpriteAnimationDrawable extends ParticleDrawable {

    private TextureRegion region;

    private float aspectRatio;

    private float globalPhase;

    private float phase;

    private class Frame {
        public float u1;
        public float v1;
        public float u2;
        public float v2;
    }

    Animation<Frame> animation;

    private float[] vertices;
    private short[] indexes;
    Vector2 origin = new Vector2();
    Vector2 tmp = new Vector2();

    public SpriteAnimationDrawable() {
        makeVertices();
    }

    public SpriteAnimationDrawable(TextureRegion region, int rows, int cols) {
        set(region, rows, cols);
        makeVertices();
    }

    private void makeVertices() {
        int attributeCount = 5;
        vertices = new float[4 * attributeCount];
        indexes = new short[2 * 3];
    }

    private void updateVertices(float x, float y, float width, float height, Color color, float u, float v, float u2, float v2, float rotation, float originX, float originY) {
        int idx = 0;
        origin.set(x + width * originX, y + width * originY);

        tmp.set(x, y).rotateAround(origin, rotation);
        vertices[idx++] = tmp.x;
        vertices[idx++] = tmp.y;
        vertices[idx++] = color.toFloatBits();
        vertices[idx++] = u;
        vertices[idx++] = v;

        tmp.set(x, y+height).rotateAround(origin, rotation);
        vertices[idx++] = tmp.x;
        vertices[idx++] = tmp.y;
        vertices[idx++] = color.toFloatBits();
        vertices[idx++] = u;
        vertices[idx++] = v2;

        tmp.set(x+width, y+height).rotateAround(origin, rotation);
        vertices[idx++] = tmp.x;
        vertices[idx++] = tmp.y;
        vertices[idx++] = color.toFloatBits();
        vertices[idx++] = u2;
        vertices[idx++] = v2;

        tmp.set(x+width, y).rotateAround(origin, rotation);
        vertices[idx++] = tmp.x;
        vertices[idx++] = tmp.y;
        vertices[idx++] = color.toFloatBits();
        vertices[idx++] = u2;
        vertices[idx++] = v;

        idx = 0;

        indexes[idx++] = 0;
        indexes[idx++] = 1;
        indexes[idx++] = 2;
        indexes[idx++] = 0;
        indexes[idx++] = 2;
        indexes[idx++] = 3;
    }

    public void set(int rows, int cols) {
        set(region, rows, cols);
    }

    public void set(TextureRegion region, int rows, int cols) {
        this.region = region;
        Array<Frame> frames = new Array<>();

        float uSize = (region.getU2()-region.getU())/cols;
        float vSize = (region.getV2()-region.getV())/rows;
        for(int r = 0; r < rows; r++) {
            for(int c = 0; c < cols; c++) {
                Frame frame = new Frame();
                frame.u1 = region.getU() + c * uSize;
                frame.u2 = region.getU() + (c+1) * uSize;
                frame.v2 = region.getV() + r * vSize;
                frame.v1  = region.getV() + (r+1) * vSize; // we are flipping the V's
                frames.add(frame);
            }
        }
        animation = new Animation<>(1f/(rows*cols), frames);
        setAspectRatio(uSize/vSize);
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height, float rotation, float originX, float originY) {
        if(batch instanceof PolygonSpriteBatch) {
            PolygonSpriteBatch polygonSpriteBatch = (PolygonSpriteBatch) batch;

            Frame frame = animation.getKeyFrame(phase, false);

            updateVertices(x-width*originX, y-height*originY, width, height, batch.getColor(), frame.u1, frame.v1, frame.u2, frame.v2, rotation, originX, originY);
            polygonSpriteBatch.draw(region.getTexture(), vertices, 0, vertices.length, indexes, 0, indexes.length);
        }

    }

    @Override
    public void draw (Batch batch, Particle particle, Color color) {
        float rotation = particle.rotation.x;
        float width = particle.size.x;
        float height = particle.size.y;
        float y = particle.getY();
        float x = particle.getX();

        draw(batch, x, y, width, height, rotation, particle.pivot.x, particle.pivot.y);
    }

    @Override
    public float getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    @Override
    public void setCurrentParticle(Particle particle) {
        phase = globalPhase + particle.seed;
        phase = phase - (int)phase; // normalize to 0-1
    }

    @Override
    public TextureRegion getTextureRegion() {
        return region;
    }

    public void setPhase(float phase) {
        globalPhase = phase;
        globalPhase = globalPhase - (int)phase; // normalize to 0-1
    }
}
