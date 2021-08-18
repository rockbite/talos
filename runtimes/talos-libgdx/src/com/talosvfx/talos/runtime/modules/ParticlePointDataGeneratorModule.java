package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticlePointData;

public abstract class ParticlePointDataGeneratorModule extends AbstractModule {

	public static final int MODULE = 0;

	public Array<ParticlePointData> pointData = new Array<>();

	public abstract void update (float delta);

	public void generateParticlePointData (Particle particle, Pool<ParticlePointData> particlePointDataPool) {
		createPoints(particle, particlePointDataPool);
	}

	protected abstract void createPoints (Particle particle, Pool<ParticlePointData> particlePointDataPool);

	public void freePoints (Pool<ParticlePointData> particlePointDataPool) {
		particlePointDataPool.freeAll(pointData);
		pointData.clear();
	}



}
