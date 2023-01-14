package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.utils.Pool;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticlePointData;
import com.talosvfx.talos.runtime.ParticlePointGroup;
import com.talosvfx.talos.runtime.values.ModuleValue;

public class SingleParticlePointDataGeneratorModule extends ParticlePointDataGeneratorModule {

	ModuleValue<ParticlePointDataGeneratorModule> outModule;

	@Override
	protected void defineSlots () {
		outModule = new ModuleValue<>();
		outModule.setModule(this);
		createOutputSlot(ParticlePointDataGeneratorModule.MODULE, outModule);
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
