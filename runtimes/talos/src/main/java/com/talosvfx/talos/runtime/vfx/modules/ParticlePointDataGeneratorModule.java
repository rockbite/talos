package com.talosvfx.talos.runtime.vfx.modules;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.talosvfx.talos.runtime.vfx.IEmitter;
import com.talosvfx.talos.runtime.vfx.Particle;
import com.talosvfx.talos.runtime.vfx.ParticlePointData;
import com.talosvfx.talos.runtime.vfx.ParticlePointGroup;

public abstract class ParticlePointDataGeneratorModule extends AbstractModule {

	public static final int MODULE = 0;


	public abstract void update (float delta);

	public void generateParticlePointData (Particle particle, Pool<ParticlePointData> particlePointDataPool, Pool<ParticlePointGroup> groupPool) {
		ParticlePointGroup obtain = groupPool.obtain();
		obtain.requester = particle.requesterID;
		obtain.seed = particle.seed;

		Array<ParticlePointGroup> pointData = particle.getEmitter().pointData();

		pointData.add(obtain);

		createPoints(particle, particlePointDataPool, obtain);
	}

	protected abstract void createPoints (Particle particle, Pool<ParticlePointData> particlePointDataPool, ParticlePointGroup group);




}
