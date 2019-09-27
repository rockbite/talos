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
	float delay;
	float delayTimer;

	boolean isContinuous = false;

	private EmitterModule emitterModule;

	float rate; // emission rate

	// inner vars
	public float alpha;
	public float particlesToEmmit;

	public boolean initialized = false;

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

	public void init () {
		position.set(0, 0);

		emitterModule = emitterGraph.getEmitterModule();
		if (emitterModule == null)
			return;

		delay = emitterModule.getDelay();
		duration = emitterModule.getDuration();
		isContinuous = emitterModule.isContinnuous();

		delayTimer = delay;


		// inner variable defaults
		alpha = 0f;
		particlesToEmmit = 0f;

		initialized = true;
	}

	public void update (float delta) {
		emitterModule = emitterGraph.getEmitterModule();
		if (emitterModule == null)
			return;

		if(!initialized) {
			init();
		}

		//update variables to their real values
		emitterModule.updateScopeData(this);

		delay = emitterModule.getDelay();
		duration = emitterModule.getDuration();
		isContinuous = emitterModule.isContinnuous();
		rate = emitterModule.getRate();

		if(delayTimer > 0) {
			delayTimer -= delta;
			if(delayTimer < 0) delayTimer = 0;
			if(delayTimer > 0) {

				updateParticles(delta); // process existing particles at least

				emitterGraph.resetRequesters();
				return;
			}
		}

		alpha += delta / duration;
		if (alpha > 1f) {
			alpha = 1f;
		}

		//update variables to their real values
		emitterModule.updateScopeData(this);

		//
		if (alpha < 1f) { // emission only here
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
		updateParticles(delta);

		if (alpha == 1f) {
			if (isContinuous) {
				// let's repeat
				restart();
			}
		}

		emitterGraph.resetRequesters();
	}

	private void updateParticles(float delta) {
		for (int i = activeParticles.size - 1; i >= 0; i--) {
			Particle particle = activeParticles.get(i);
			particle.update(delta);
			if (particle.alpha >= 1f) {
				particlePool.free(particle);
				activeParticles.removeIndex(i);
			}
		}
	}

	public void restart() {
    	delayTimer = delay;
    	alpha = 0;
	}

    public void setScope (ScopePayload scope) {
        emitterGraph.setScope(scope);
    }

    public ParticleEffectInstance getEffect() {
    	return parentParticleInstance;
	}
}
