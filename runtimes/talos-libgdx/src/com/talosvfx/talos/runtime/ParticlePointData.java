package com.talosvfx.talos.runtime;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.talosvfx.talos.runtime.modules.ParticleModule;

public class ParticlePointData {

	public float alpha;
	public float seed;
	public float x, y;
	public Vector3 rotation = new Vector3();
	public float transparency;
	public Color color = new Color();

	public int pointDataIndex;

	public void setFromParticle (Particle particle) {
		setFromParticle(particle, 0, 0);
	}

	public void setFromParticle (Particle particle, Vector2 positionOverride) {
		setFromParticle(particle, positionOverride.x, positionOverride.y);
	}

	public void setFromParticle (Particle particle, float positionOverrideX, float positionOverrideY) {
		ParticleModule particleModule = particle.getEmitter().getParticleModule();

		Vector3 rotation = particleModule.getRotation();
		Color color = particleModule.getColor();
		float transparency = particleModule.getTransparency();


		this.x = positionOverrideX ;
		this.y = positionOverrideY;
		this.x += particle.position.x;
		this.y += particle.position.y;


		this.rotation.set(rotation);

		this.color.set(color);

		this.transparency = transparency;
	}
}
