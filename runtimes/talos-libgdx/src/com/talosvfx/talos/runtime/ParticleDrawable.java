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

package com.talosvfx.talos.runtime;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class ParticleDrawable {

    public abstract void draw(Batch batch, float x, float y, float width, float height, float rotation, float originX, float originY);

    public abstract void draw(Batch batch, Particle particle, Color color);

    public void draw (Batch batch, ParticlePointData particlePointData, Color color) {

    }

    public abstract float getAspectRatio();

    public abstract void setCurrentParticle(Particle particle);

    public abstract TextureRegion getTextureRegion();

    public void notifyCreate(Particle particle) {

    }

    public void notifyDispose(Particle particle) {

    }
}
