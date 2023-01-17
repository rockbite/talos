package com.talosvfx.talos.runtime.vfx.modules;

import com.badlogic.gdx.utils.Pool;
import com.talosvfx.talos.runtime.vfx.Particle;
import com.talosvfx.talos.runtime.vfx.ParticlePointData;
import com.talosvfx.talos.runtime.vfx.ParticlePointGroup;
import com.talosvfx.talos.runtime.vfx.values.ModuleValue;

public class SingleParticlePointDataGeneratorModule extends ParticlePointDataGeneratorModule {

	ModuleValue<ParticlePointDataGeneratorModule> outModule;

	@Override
	protected void defineSlots () {
		outModule = new ModuleValue<>();
		outModule.setModule(this);
		createOutputSlot(MODULE, outModule);
	}

	@Override
	public void processCustomValues () {
	}

	@Override
	public void update (float delta) {

	}

	@Override
	protected void createPoints (Particle particle, Pool<ParticlePointData> particlePointDataPool, ParticlePointGroup group) {
		ParticlePointData obtain = particlePointDataPool.obtain();
		obtain.setFromParticle(particle);
		obtain.pointDataIndex = 0;
		obtain.alpha = particle.alpha;
		group.pointDataArray.add(obtain);

	}
}
