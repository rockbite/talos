package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class ParticleEmitter {

    ParticleSystem particleSystem;

    Vector2 position = new Vector2();
    float duration;

    private EmitterModule emitterModule;

    float rate; // emission rate

    // inner vars
    float alpha;
    float particlesToEmmit;

    public Array<Particle> activeParticles = new Array<>();

    private final Pool<Particle> particlePool = new Pool<Particle>() {
        @Override
        protected Particle newObject() {
            return new Particle();
        }
    };

    public ParticleEmitter(ParticleSystem particleSystem) {
        this.particleSystem = particleSystem;
        this.emitterModule = null;
        init();
    }

    public ParticleEmitter(ParticleSystem particleSystem, EmitterModule emitterModule) {
        this.particleSystem = particleSystem;
        this.emitterModule = emitterModule;
        init();
    }

    public void setEmitterModule(EmitterModule emitterModule) {
        this.emitterModule = emitterModule;
    }

    public void init() {
        position.set(0, 0);
        duration = 2f;

        // inner variable defaults
        alpha = 0f;
        particlesToEmmit = 0f;
    }

    public void update(float delta) {
        alpha += delta/duration;
        if(alpha > 1f) alpha = 1f;

        if(emitterModule == null) return;

        //update variables to their real values
        emitterModule.updateScopeData(this);
        rate = emitterModule.getRate();

        //
        if(alpha < 1f) {
            // let's emmit
            particlesToEmmit += rate * delta;

            int count = (int) particlesToEmmit;
            for (int i = 0; i < count; i++) {
                Particle particle = particlePool.obtain();
                if(particleSystem.getParticleModule() != null) {
                    particle.init(particleSystem.getParticleModule(), this);
                    activeParticles.add(particle);
                }
            }
            particlesToEmmit -= count;
        }

        // process existing particles.
        for(int i = activeParticles.size - 1; i >= 0 ; i--) {
            Particle particle = activeParticles.get(i);
            particle.update(delta);
            if(particle.alpha >= 1f) {
                particlePool.free(particle);
                activeParticles.removeIndex(i);
            }
        }


        if(alpha == 1f) {
            // let's repeat
            alpha = 0;
        }
    }

}
