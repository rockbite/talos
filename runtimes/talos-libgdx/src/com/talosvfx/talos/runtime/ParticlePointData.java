package com.talosvfx.talos.runtime;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class ParticlePointData {

	public float x, y;
	public Vector3 rotation = new Vector3();
	public Vector2 size = new Vector2();
	public float transparency;
	public Color color = new Color();

	public int pointDataIndex;

	public void setFromParticle (Particle particle) {
		this.x = particle.getX();
		this.y = particle.getY();
		this.rotation = particle.rotation;
		this.size.set(particle.size);

		this.color.set(particle.color);
		this.transparency = particle.transparency;
	}

	public void setFromParticle (Particle particle, Vector2 positionOverride) {
		this.x = positionOverride.x + particle.getX();
		this.y = positionOverride.y + particle.getY();
		this.rotation = particle.rotation;
		this.size.set(particle.size);

		this.color.set(particle.color);
		this.transparency = particle.transparency;
	}
}
