package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.rockbite.tools.talos.runtime.modules.EmitterModule;

public class ParticleEmitterInstance {

    private final ParticleEffectInstance parentParticleInstance;
	public boolean isComplete = false;
	ParticleEmitterDescriptor emitterGraph;

	Vector2 position = new Vector2();
	float duration;
	float delay;
	float delayTimer;

	public boolean isVisible = true;
	boolean paused = false;
	boolean isContinuous = false;
	boolean isAttached = false;

	public Color tint = new Color(Color.WHITE);

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
		isComplete = false;
		particlesToEmmit = 1f; // always emmit one first

		initialized = true;
	}

	public void update (float delta) {
		emitterModule = emitterGraph.getEmitterModule();
		if (emitterModule == null)
			return;

		if(!initialized) {
			init();
		}

		if(paused) return;

		//update variables to their real values
		emitterModule.updateScopeData(this);

		delay = emitterModule.getDelay();
		duration = emitterModule.getDuration();
		isContinuous = emitterModule.isContinnuous();
		rate = emitterModule.getRate();
		isAttached = emitterModule.isAttached();

		if(delayTimer > 0) {
			delayTimer -= delta;
			if(delayTimer < 0) delayTimer = 0;
			if(delayTimer > 0) {

				updateParticles(delta); // process existing particles at least

				emitterGraph.resetRequesters();
				return;
			}
		}

		float normDelta = delta/duration;

		float deltaLeftover = 0;
		if(alpha + normDelta > 1f) {
			deltaLeftover = (1f - alpha) * duration;
			alpha = 1f;
		} else {
			alpha += normDelta;
			deltaLeftover = delta;
		}

		//update variables to their real values
		emitterModule.updateScopeData(this);

		//
		if (alpha < 1f || (alpha == 1f && deltaLeftover > 0)) { // emission only here
			// let's emmit
			particlesToEmmit += rate * deltaLeftover;

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

		if(activeParticles.size == 0) {
			isComplete = true;
		} else {
			isComplete = false;
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
    	isComplete = false;
		particlesToEmmit = 1f;
	}

    public void setScope (ScopePayload scope) {
        emitterGraph.setScope(scope);
    }

    public ParticleEffectInstance getEffect() {
    	return parentParticleInstance;
	}

	public boolean isAttached() {
		return isAttached;
	}

	public void stop() {
		alpha = 1f;
	}

	public void pause() {
		paused = true;
	}

	public void resume() {
		paused = false;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	public void setTint(float r, float g, float b, float a) {
		tint.set(r, g, b, a);
	}

	public void setTint(Color color) {
    	tint.set(color);
	}
}
