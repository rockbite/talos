package com.talosvfx.talos.runtime;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.talosvfx.talos.runtime.modules.ParticleModule;

public class ParticlePointData {

	public Particle reference;
	public float alpha;
	public float x, y, z;

	public int pointDataIndex;

	public void setFromParticle (Particle particle) {
		setFromParticle(particle, 0, 0, 0);
	}

	public void setFromParticle (Particle particle, Vector3 positionOverride) {
		setFromParticle(particle, positionOverride.x, positionOverride.y, positionOverride.z);
	}

	public void setFromParticle (Particle particle, float positionOverrideX, float positionOverrideY, float positionOverrideZ) {
		this.x = positionOverrideX ;
		this.y = positionOverrideY;
		this.z = positionOverrideZ;
		this.x += particle.position.x;
		this.y += particle.position.y;
		this.z += particle.position.z;
		this.reference = particle;
	}
}
