package com.talosvfx.talos.runtime;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.talosvfx.talos.runtime.modules.ParticleModule;

public class ParticlePointData {

	public Particle reference;
	public float alpha;
	public float x, y;

	public int pointDataIndex;

	public void setFromParticle (Particle particle) {
		setFromParticle(particle, 0, 0);
	}

	public void setFromParticle (Particle particle, Vector2 positionOverride) {
		setFromParticle(particle, positionOverride.x, positionOverride.y);
	}

	public void setFromParticle (Particle particle, float positionOverrideX, float positionOverrideY) {
		this.x = positionOverrideX ;
		this.y = positionOverrideY;
		this.x += particle.position.x;
		this.y += particle.position.y;
		this.reference = particle;
	}
}
