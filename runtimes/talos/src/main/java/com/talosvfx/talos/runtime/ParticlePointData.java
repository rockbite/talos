package com.talosvfx.talos.runtime;

import com.badlogic.gdx.math.Vector3;

public class ParticlePointData {

	public Particle reference;
	public float alpha;
	public float x, y, z;

	public int pointDataIndex;


	public void setFromParticle (Particle particle) {
		setFromParticle(particle, 0, 0, 0, false);
	}

	public void setFromParticle (Particle particle, Vector3 positionOverride) {
		setFromParticle(particle, positionOverride.x, positionOverride.y, positionOverride.z, false);
	}

	public void setFromParticle (Particle particle, Vector3 positionOverride, boolean useAbsolutePosition) {
		setFromParticle(particle, positionOverride.x, positionOverride.y, positionOverride.z, useAbsolutePosition);
	}

	public void setFromParticle (Particle particle, float positionOverrideX, float positionOverrideY, float positionOverrideZ, boolean useAbsolutePosition) {
		this.x = positionOverrideX ;
		this.y = positionOverrideY;
		this.z = positionOverrideZ;
		if (useAbsolutePosition) {

		} else {
			this.x += particle.getX();
			this.y += particle.getY();
			this.z += particle.getZ();
		}
		this.reference = particle;
	}
}
