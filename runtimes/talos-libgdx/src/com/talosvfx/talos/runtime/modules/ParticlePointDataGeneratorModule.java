package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticlePointData;
import com.talosvfx.talos.runtime.ParticlePointGroup;

public abstract class ParticlePointDataGeneratorModule extends AbstractModule {

	public static final int MODULE = 0;

	public Array<ParticlePointGroup> pointData = new Array<>();

	public abstract void update (float delta);

	public void generateParticlePointData (Particle particle, Pool<ParticlePointData> particlePointDataPool, Pool<ParticlePointGroup> groupPool) {
		ParticlePointGroup obtain = groupPool.obtain();
		obtain.requester = particle.requesterID;
		obtain.seed = particle.seed;
		pointData.add(obtain);
		createPoints(particle, particlePointDataPool, obtain);
	}

	protected abstract void createPoints (Particle particle, Pool<ParticlePointData> particlePointDataPool, ParticlePointGroup group);

	public void freePoints (Pool<ParticlePointData> particlePointDataPool, Pool<ParticlePointGroup> groupPool) {
		for (ParticlePointGroup group : pointData) {
			particlePointDataPool.freeAll(group.pointDataArray);
			group.pointDataArray.clear();
			particlePointDataPool.clear();

			groupPool.free(group);
		}
		pointData.clear();
	}



}
