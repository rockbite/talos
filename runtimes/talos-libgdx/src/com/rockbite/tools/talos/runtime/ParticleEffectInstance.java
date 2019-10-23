package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.rockbite.tools.talos.runtime.render.ParticleRenderer;

import java.util.Comparator;

public class ParticleEffectInstance {

    private final ParticleEffectDescriptor descriptor;

    private Array<ParticleEmitterInstance> emitters = new Array<>();

    Vector2 position = new Vector2();

    ScopePayload scopePayload = new ScopePayload();

    public boolean loopable = false;

    int particleCount = 0;
    public int nodeCalls = 0;

    private float totalTime = 0;

    private EmitterComparator emitterComparator = new EmitterComparator();

    public class EmitterComparator implements Comparator<ParticleEmitterInstance> {

		@Override
		public int compare(ParticleEmitterInstance o1, ParticleEmitterInstance o2) {
			return o1.emitterGraph.getSortPosition() - o2.emitterGraph.getSortPosition();
		}
	}

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
    	descriptor.setEffectReference(this);

		if(totalTime > 3600) totalTime = 0;
		totalTime += delta;

		if(scopePayload != null) {
			scopePayload.set(ScopePayload.TOTAL_TIME, totalTime);
		}

		particleCount = 0;
		nodeCalls = 0;
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
    	loopable = false;
		for (int i = 0; i < emitters.size; i++) {
			emitters.get(i).stop();
		}
	}

	public void pause() {
		for (int i = 0; i < emitters.size; i++) {
			emitters.get(i).pause();
		}
	}

	public void resume() {
		for (int i = 0; i < emitters.size; i++) {
			emitters.get(i).resume();
		}
	}


	public void restart () {
		for (int i = 0; i < emitters.size; i++) {
			emitters.get(i).restart();
		}
	}



	public Array<ParticleEmitterInstance> getEmitters () {
        return emitters;
    }

    public ParticleEmitterInstance getEmitter(ParticleEmitterDescriptor descriptor) {
    	for(ParticleEmitterInstance instance: emitters) {
    		if(instance.emitterGraph == descriptor) {
    			return instance;
			}
		}

    	return null;
	}


	public void setPosition(float x, float y) {
		position.set(x, y);
	}

	public Vector2 getPosition() {
		return position;
	}

	public int getParticleCount() {
		return particleCount;
	}

	public int getNodeCalls() {
		return nodeCalls;
	}

	public void reportNodeCall() {
		nodeCalls++;
	}

	public void sortEmitters() {
		emitters.sort(emitterComparator);
		for(int i = 0; i < emitters.size; i++) {
			emitters.get(i).emitterGraph.setSortPosition(i);
		}
	}
}
