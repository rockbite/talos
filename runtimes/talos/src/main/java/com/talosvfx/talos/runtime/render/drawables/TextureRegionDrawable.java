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
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticleDrawable;
import com.talosvfx.talos.runtime.ParticlePointData;

public class TextureRegionDrawable extends ParticleDrawable {

    private Sprite region;

    public TextureRegionDrawable() {

    }

    public TextureRegionDrawable(Sprite region) {
        this.region = region;
    }

    public void setRegion(Sprite region) {
        this.region = region;
    }

    @Override
    public void draw(Batch batch, Particle particle, Color color) {
		Vector3 rotation = particle.rotation;
		float width = particle.size.x;
		float height = particle.size.y;
		float y = particle.getY();
		float x = particle.getX();

		if(region == null) return;

		region.setColor(color);

		draw(batch, x, y, width, height, rotation.x, particle.pivot.x, particle.pivot.y);
	}

	@Override
	public void draw (Batch batch, ParticlePointData particlePointData, Color color) {

		float width = 1f;
		float height = 1f;

		float x = particlePointData.x;
		float y = particlePointData.y;

		if(region == null) return;

		region.setColor(color);
//		region.setAlpha(particlePointData.transparency);

//		draw(batch, x, y, width, height, particlePointData.rotation.x);
	}

	@Override
	public void draw (Batch batch, float x, float y, float width, float height, float rotation, float originX, float originY) {
		region.setPosition(x - width * originX, y - height * originY);
		region.setSize(width, height) ;
		region.setOrigin(width * originX, height * originY);
		region.setRotation(rotation);
		region.draw(batch);
	}

	@Override
    public float getAspectRatio() {
        if(region == null) {
            return 1;
        }
        return region.getRegionWidth()/ (float)region.getRegionHeight();
    }

    @Override
    public void setCurrentParticle(Particle particle) {

    }

    @Override
    public TextureRegion getTextureRegion() {
        return region;
    }
}
