/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.talosvfx.talos.runtime;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.render.ParticleRenderer;
import com.talosvfx.talos.runtime.simulation.TinyEmitter;

import java.util.Comparator;

public class ParticleEffectInstance {

    private final ParticleEffectDescriptor descriptor;

    private Array<IEmitter> emitters = new Array<>();

    Vector3 position = new Vector3();

    ScopePayload scopePayload = new ScopePayload();

    public boolean loopable = false;

    int particleCount = 0;
    public int nodeCalls = 0;

    private float totalTime = 0;

    private EmitterComparator emitterComparator = new EmitterComparator();

    public float alpha = 1f;

    private boolean paused = false;

	public void init () {
		for (int i = 0; i < emitters.size; i++) {
			emitters.get(i).init();
		}
	}

	public float getTotalTime () {
		return totalTime;
	}

	public boolean isPaused () {
		return paused;
	}

	public class EmitterComparator implements Comparator<IEmitter> {

		@Override
		public int compare(IEmitter o1, IEmitter o2) {
			return o1.getEmitterGraph().getSortPosition() - o2.getEmitterGraph().getSortPosition();
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

	public ScopePayload getScope() {
    	return scopePayload;
	}

	public void update (float delta) {
		if(paused) return;

		if(isComplete() && !loopable) return;

    	descriptor.setEffectReference(this);

		if(totalTime > 3600) totalTime = 0; //TODO: maybe just supple TimeUtils time now instead...
		totalTime += delta;

		if(scopePayload != null) {
			scopePayload.set(ScopePayload.TOTAL_TIME, totalTime);
		}

		particleCount = 0;
		nodeCalls = 0;
		for (int i = 0; i < emitters.size; i++) {
			emitters.get(i).update(delta);
			particleCount += emitters.get(i).getActiveParticleCount();
		}

		if(particleCount == 0 && loopable) {
			for (int i = 0; i < emitters.size; i++) {
				if(!emitters.get(i).isContinuous()) {
					if(emitters.get(i).getDelayRemaining() == 0) {
						emitters.get(i).restart();
					}
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

    public void addAdvancedEmitter(ParticleEmitterDescriptor particleEmitterDescriptor) {
		final TinyEmitter emitter = new TinyEmitter(particleEmitterDescriptor, this);
		emitters.add(emitter);
	}

	public void removeEmitterForEmitterDescriptor (ParticleEmitterDescriptor emitter) {
		for (int i = emitters.size - 1; i >= 0; i--) {
			if (emitters.get(i).getEmitterGraph() == emitter) {
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
			if (!emitters.get(i).isComplete()) {
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
		paused = true;
	}

	public void resume() {
		for (int i = 0; i < emitters.size; i++) {
			emitters.get(i).resume();
		}
		paused = false;
	}


	public void restart () {
		for (int i = 0; i < emitters.size; i++) {
			emitters.get(i).restart();
		}
		paused = false;
		totalTime = 0;
	}



	public Array<IEmitter> getEmitters () {
        return emitters;
    }

    public IEmitter getEmitter(ParticleEmitterDescriptor descriptor) {
    	for(IEmitter instance: emitters) {
    		if(instance.getEmitterGraph() == descriptor) {
    			return instance;
			}
		}

    	return null;
	}


	public void setPosition(float x, float y, float z) {
		position.set(x, y, z);
	}

	public Vector3 getPosition() {
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
			emitters.get(i).getEmitterGraph().setSortPosition(i);
		}
	}
}
