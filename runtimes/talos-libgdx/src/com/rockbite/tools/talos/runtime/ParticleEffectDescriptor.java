package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.utils.Array;

public class ParticleEffectDescriptor {

	/**
	 * graph per each emitter
	 */
	private Array<ParticleEmitterDescriptor> emitterModuleGraphs = new Array<>();

	public ParticleEffectDescriptor () {

	}

	public void addEmitter (ParticleEmitterDescriptor emitter) {
	    emitterModuleGraphs.add(emitter);
    }

	public void removeEmitter (ParticleEmitterDescriptor emitter) {
		emitterModuleGraphs.removeValue(emitter, true);
	}

	public ParticleEmitterDescriptor createEmitterDescriptor () {
		return new ParticleEmitterDescriptor(this);
	}
}
