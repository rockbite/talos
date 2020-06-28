package com.talosvfx.talos.runtime.simulation;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.ParticleEmitterDescriptor;
import com.talosvfx.talos.runtime.ScopePayload;
import com.talosvfx.talos.runtime.modules.EmitterModule;
import com.talosvfx.talos.runtime.modules.ParticleModule;


public class TinyEmitter {

    private ParticleEmitterDescriptor emitterGraph;
    private EmitterModule emitterModule;
    private ParticleModule particleModule;

    private float duration;
    private boolean isContinuous = false;
    private float rate;
    private float alpha;
    private float particlesToEmmit;

    private float time = 0;
    private Array<ParticleRecord> timeline = new Array();

    public TinyEmitter(ParticleEmitterDescriptor moduleGraph) {
        this.emitterGraph = moduleGraph;
    }

    public void simulate() {
        System.out.println("starting tiny particle simulation");
        init();
        float step = 1/30f; // simulating 30 frames per second
        while (alpha < 0) {
            update(step);
        }
        System.out.println("time at the end: "+ time);
        System.out.println("timeline size: "+ timeline.size);
    }

    private void init() {
        emitterModule = emitterGraph.getEmitterModule();
        particleModule = emitterGraph.getParticleModule();
        if (emitterModule == null)
            return;

        duration = emitterModule.getDuration();
        isContinuous = emitterModule.isContinuous();

        alpha = 0f;
        particlesToEmmit = 1f; // always emmit one first

        timeline.clear();
    }

    private void update (float delta) {
        emitterModule.getScope().set(ScopePayload.EMITTER_ALPHA, alpha);
        emitterModule.getScope().set(ScopePayload.REQUESTER_ID, 1.1f);
        duration = emitterModule.getDuration();
        isContinuous = emitterModule.isContinuous();
        rate = emitterModule.getRate();

        float normDelta = delta/duration;

        float deltaLeftover = 0;
        if(alpha + normDelta > 1f) {
            deltaLeftover = (1f - alpha) * duration;
            alpha = 1f;
        } else {
            alpha += normDelta;
            deltaLeftover = delta;
        }

        emitterModule.getScope().set(ScopePayload.EMITTER_ALPHA, alpha);

        if (alpha < 1f || (alpha == 1f && deltaLeftover > 0)) { // emission only here
            // let's emmit
            particlesToEmmit += rate * deltaLeftover;

            int count = (int)particlesToEmmit;
            for (int i = 0; i < count; i++) {
                if (emitterGraph.getParticleModule() != null) {
                    // emit a particle here and record it's data
                    emitParticle();
                }
            }
            particlesToEmmit -= count;
        }

        emitterGraph.resetRequesters();

        time += delta;
    }

    /**
     * really not gonna, just pretending
     */
    private void emitParticle() {
        float seed = MathUtils.random();
        particleModule.getScope().set(ScopePayload.EMITTER_ALPHA, alpha);
        particleModule.getScope().set(ScopePayload.PARTICLE_ALPHA, 0);
        particleModule.getScope().set(ScopePayload.PARTICLE_SEED, seed);
        particleModule.getScope().set(ScopePayload.REQUESTER_ID, seed);
        particleModule.getScope().set(ScopePayload.EMITTER_ALPHA_AT_P_INIT, alpha);

        float life = particleModule.getLife();

        ParticleRecord record = new ParticleRecord();
        record.start = time;
        record.end = time + life;
        record.seed = seed;

        timeline.add(record);
    }

    private class ParticleRecord {
        public float start;
        public float end;
        public float seed;
    }
}
