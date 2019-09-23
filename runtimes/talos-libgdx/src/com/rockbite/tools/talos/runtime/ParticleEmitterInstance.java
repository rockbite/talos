package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.rockbite.tools.talos.runtime.modules.EmitterModule;

public class ParticleEmitterInstance {

    private final ParticleEffectInstance parentParticleInstance;
    ParticleEmitterDescriptor emitterGraph;

	Vector2 position = new Vector2();
	float duration;

	private EmitterModule emitterModule;

	float rate; // emission rate

	// inner vars
	public float alpha;
	public float particlesToEmmit;

	public Array<Particle> activeParticles = new Array<>();

	private final Pool<Particle> particlePool = new Pool<Particle>() {
		@Override
		protected Particle newObject () {
			return new Particle();
		}
	};

    public ParticleEmitterInstance (ParticleEmitterDescriptor moduleGraph, ParticleEffectInstance particleEffectInstance) {
		this.emitterGraph = moduleGraph;
        parentParticleInstance = particleEffectInstance;
        setScope(particleEffectInstance.scopePayload); //Default set to the parent payload instance
        init();
	}

	public void setEmitterModule (EmitterModule emitterModule) {
		this.emitterModule = emitterModule;
	}

	public void init () {
		position.set(0, 0);
		duration = 2f;

		// inner variable defaults
		alpha = 0f;
		particlesToEmmit = 0f;
	}

	public void update (float delta) {
		alpha += delta / duration;
		if (alpha > 1f)
			alpha = 1f;

		emitterModule = emitterGraph.getEmitterModule();
		if (emitterModule == null)
			return;

		//update variables to their real values
		emitterModule.updateScopeData(this);
		rate = emitterModule.getRate();

		//
		if (alpha < 1f) {
			// let's emmit
			particlesToEmmit += rate * delta;

			int count = (int)particlesToEmmit;
			for (int i = 0; i < count; i++) {
				Particle particle = particlePool.obtain();
				if (emitterGraph.getParticleModule() != null) {
					particle.init(emitterGraph.getParticleModule(), this);
					activeParticles.add(particle);
				}
			}
			particlesToEmmit -= count;
		}

		// process existing particles.
		for (int i = activeParticles.size - 1; i >= 0; i--) {
			Particle particle = activeParticles.get(i);
			particle.update(delta);
			if (particle.alpha >= 1f) {
				particlePool.free(particle);
				activeParticles.removeIndex(i);
			}
		}

		if (alpha == 1f) {
			// let's repeat
			alpha = 0;
		}

		emitterGraph.resetRequesters();
	}

    public void setScope (ScopePayload scope) {
        emitterGraph.setScope(scope);
    }
}
