package com.talosvfx.talos.runtime;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class ParticlePointData {

	public float x, y;
	public float transparency;
	public Color color;

	public int pointDataIndex;

	public void setFromParticle (Particle particle) {
		this.x = particle.getX();
		this.y = particle.getY();

		this.color = particle.color;
		this.transparency = particle.transparency;
	}

	public void setFromParticle (Particle particle, Vector2 positionOverride) {
		this.x = positionOverride.x + particle.getX();
		this.y = positionOverride.y + particle.getY();

		this.color = particle.color;
		this.transparency = particle.transparency;
	}
}
