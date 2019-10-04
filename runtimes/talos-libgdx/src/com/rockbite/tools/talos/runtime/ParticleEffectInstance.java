package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.rockbite.tools.talos.runtime.render.ParticleRenderer;

public class ParticleEffectInstance {

    private final ParticleEffectDescriptor descriptor;

    private Array<ParticleEmitterInstance> emitters = new Array<>();

    Vector2 position = new Vector2();

    ScopePayload scopePayload = new ScopePayload();

    public boolean loopable = false;

    public ParticleEffectInstance (ParticleEffectDescriptor particleEffectDescriptor) {
        this.descriptor = particleEffectDescriptor;
    }

	public void setScope (ScopePayload scope) {
        this.scopePayload = scope;
		for (int i = 0; i < emitters.size; i++) {
			emitters.get(i).setScope(scope);
		}
	}

	public void update (float delta) {
		int particleCount = 0;
		for (int i = 0; i < emitters.size; i++) {
			emitters.get(i).update(delta);
			particleCount += emitters.get(i).activeParticles.size;
		}

		if(particleCount == 0 && loopable) {
			for (int i = 0; i < emitters.size; i++) {
				if(!emitters.get(i).isContinuous) {
					emitters.get(i).restart();
				}
			}
		}
	}

	public void render (ParticleRenderer particleRenderer) {
		particleRenderer.render(this);
	}

    public void addEmitter (ParticleEmitterDescriptor particleEmitterDescriptor) {
        final ParticleEmitterInstance particleEmitterInstance = new ParticleEmitterInstance(particleEmitterDescriptor, this);
        emitters.add(particleEmitterInstance);
    }

	public void removeEmitterForEmitterDescriptor (ParticleEmitterDescriptor emitter) {
		for (int i = emitters.size - 1; i >= 0; i--) {
			if (emitters.get(i).emitterGraph == emitter) {
				emitters.removeIndex(i);
			}
		}
	}


	public boolean isContinuous() {
		for (ParticleEmitterDescriptor emitterDescriptor: descriptor.emitterModuleGraphs) {
			if (emitterDescriptor.isContinuous()) {
				return true;
			}
		}

		return false;
	}

	public boolean isComplete() {
    	if(loopable) return false;

		for (int i = 0; i < emitters.size; i++) {
			if (!emitters.get(i).isComplete) {
				return false;
			}
		}

		return true;
	}

	public void allowCompletion() {
		for (int i = 0; i < emitters.size; i++) {
			emitters.get(i).stop();
		}
	}


	public Array<ParticleEmitterInstance> getEmitters () {
        return emitters;
    }


	public void setPosition(float x, float y) {
		position.set(x, y);
	}

	public Vector2 getPosition() {
		return position;
	}
}
