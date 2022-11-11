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
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticleDrawable;

public class PolylineRenderer extends ParticleDrawable {

    Particle particleRef;
    int interpolationPointCount;

    public Pool<Polyline> polylinePool = new Pool<Polyline>() {
        @Override
        protected Polyline newObject() {
            return new Polyline();
        }
    };
    private TextureRegion region;

    ObjectMap<Particle, Polyline> polylineMap = new ObjectMap<>();
    Array<Particle> tmpArr = new Array<>();

    @Override
    public void draw(Batch batch, float x, float y, float width, float height, float rotation, float originX, float originY) {
        Polyline polyline = polyline();
        polyline.set(width, rotation);
        polyline.draw(batch, region, x, y, null);

        tmpArr.clear();
        for(Particle key: polylineMap.keys()) {
            if(key.alpha == 1f) {
                tmpArr.add(key);
            }
        }
        for(int i = 0; i < tmpArr.size; i++) {
            if(polylineMap.containsKey(tmpArr.get(i))) {
                polylinePool.free(polylineMap.get(tmpArr.get(i)));
            }
            polylineMap.remove(tmpArr.get(i));
        }
    }

    @Override
    public void draw (Batch batch, Particle particle, Color color) {
        Vector3 rotation = particle.rotation;
        float width = particle.size.x;
        float height = particle.size.y;
        float y = particle.getY();
        float x = particle.getX();

        draw(batch, x, y, width, height, rotation.x, particle.pivot.x, particle.pivot.y);
    }

    @Override
    public float getAspectRatio() {
        return 1f;
    }

    @Override
    public void setCurrentParticle (Particle particle) {
        this.particleRef = particle;
    }

    @Override
    public TextureRegion getTextureRegion() {
        return region;
    }

    public void setRegion(TextureRegion region) {
        this.region = region;
    }

    public void setCount(int count) {
        interpolationPointCount = count;
        // reset all existing items from the pool
        polylinePool.freeAll(polylineMap.values().toArray());
        polylineMap.clear();
    }

    public void setPointData(int pointIndex, float offsetX, float offsetY, float thickness, Color color) {
        Polyline polyline = polyline();
        polyline.setPointData(pointIndex, offsetX, offsetY, thickness, color);
    }

    private Polyline polyline() {
        if(polylineMap.get(particleRef) == null) {
            Polyline polyline = polylinePool.obtain();
            polyline.initPoints(interpolationPointCount, particleRef.getX(), particleRef.getY());
            polylineMap.put(particleRef, polyline);
        }

        return polylineMap.get(particleRef);
    }

    public void setTangents(float leftX, float leftY, float rightX, float rightY) {
        Polyline polyline = polyline();
        polyline.leftTangent.set(leftX, leftY);
        polyline.rightTanget.set(rightX, rightY);
    }
}
