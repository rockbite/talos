package com.talosvfx.talos.runtime.simulation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;
import com.talosvfx.talos.runtime.IEmitter;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticleEffectInstance;
import com.talosvfx.talos.runtime.ParticleEmitterDescriptor;
import com.talosvfx.talos.runtime.ScopePayload;
import com.talosvfx.talos.runtime.modules.DrawableModule;
import com.talosvfx.talos.runtime.modules.EmitterModule;
import com.talosvfx.talos.runtime.modules.ParticleModule;

public class TinyEmitter implements IEmitter {

    private final ParticleEffectInstance parentParticleInstance;
    private ParticleEmitterDescriptor emitterGraph;
    private EmitterModule emitterModule;
    private DrawableModule drawableModule;
    private ParticleModule particleModule;
    private ScopePayload scopePayload;

    private float delay;
    private float delayTimer;
    private float duration;
    private float rate;
    private float alpha;
    private float particlesToEmmit;

    private float timePassed = 0;
    private float cursor = 0;
    private Array<ParticleRecord> timeline = new Array();

    private Array<ParticleRecord> tmp = new Array();
    private Array<Particle> activeParticles = new Array();
    public ObjectMap<ParticleRecord, Particle> newMap = new ObjectMap();
    public ObjectMap<ParticleRecord, Particle> recordMap = new ObjectMap();
    private final Pool<Particle> particlePool = new Pool<Particle>() {
        @Override
        protected Particle newObject () {
            return new Particle();
        }
    };

    Vector3 effectPosition = new Vector3();
    public Color tint = new Color(Color.WHITE);

    private boolean isVisible = true;
    private boolean paused = false;
    private boolean isContinuous = false;
    private boolean isAttached = false;
    private boolean isComplete = false;
    public boolean isAdditive = true;
    private boolean isStopped = false;
    private boolean isBlendAdd = false;

    public TinyEmitter(ParticleEmitterDescriptor moduleGraph, ParticleEffectInstance particleEffectInstance) {
        this.emitterGraph = moduleGraph;
        parentParticleInstance = particleEffectInstance;
        setScope(particleEffectInstance.getScope()); //Default set to the parent payload instance
        init();
    }

    public void simulate() {
        if(emitterGraph == null) return;
        timeline.clear();
        timePassed = 0;
        long startTime = TimeUtils.nanoTime();
        System.out.println("starting tiny particle simulation");

        if(emitterModule == null) return;
        float step = 1/30f; // simulating 30 frames per second
        while (alpha < 1f) {
            updateSimulation(step);
        }
        long endTime = TimeUtils.nanoTime();
        System.out.println("time at the end: "+ timePassed);
        System.out.println("timeline size: "+ timeline.size);
        System.out.println();

        System.out.println("Simulation complete in: " + TimeUtils.nanosToMillis(endTime - startTime) + "ms");
    }

    public void init() {
        emitterModule = emitterGraph.getEmitterModule();
        particleModule = emitterGraph.getParticleModule();
        drawableModule = emitterGraph.getDrawableModule();
        if (emitterModule == null)
            return;

        duration = emitterModule.getDuration();
        delay = emitterModule.getDelay();
        isContinuous = emitterModule.isContinuous();

        delayTimer = delay;

        alpha = 0f;
        particlesToEmmit = 1f; // always emmit one first
        isComplete = false;

        simulate();
    }

    public void updateSimulation (float delta) {
        emitterModule.getScope().set(ScopePayload.EMITTER_ALPHA, alpha);
        emitterModule.getScope().setCurrentRequesterID(emitterModule.getScope().newParticleRequester());
        duration = emitterModule.getDuration();
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

        timePassed += delta;
    }

    @Override
    public ParticleEmitterDescriptor getEmitterGraph () {
        return emitterGraph;
    }

    @Override
    public boolean isVisible () {
        return isVisible;
    }

    @Override
    public boolean isAdditive () {
        return isAdditive;
    }

    @Override
    public boolean isBlendAdd () {
        return isBlendAdd;
    }

    @Override
    public Array<Particle> getActiveParticles () {
        return activeParticles;
    }

    /**
     * really not gonna, just pretending
     */
    private void emitParticle() {
        float seed = MathUtils.random();
        particleModule.getScope().set(ScopePayload.EMITTER_ALPHA, alpha);
        particleModule.getScope().set(ScopePayload.PARTICLE_ALPHA, 0);
        particleModule.getScope().set(ScopePayload.PARTICLE_SEED, seed);
        particleModule.getScope().setCurrentRequesterID(particleModule.getScope().newParticleRequester());
        particleModule.getScope().set(ScopePayload.EMITTER_ALPHA_AT_P_INIT, alpha);

        float life = particleModule.getLife();

        ParticleRecord record = new ParticleRecord();
        record.start = timePassed;
        record.end = timePassed + life;
        record.seed = seed;

        timeline.add(record);
    }

    @Override
    public float getAlpha () {
        return alpha;
    }

    @Override
    public ParticleModule getParticleModule () {
        return particleModule;
    }

    @Override
    public EmitterModule getEmitterModule () {
        return emitterModule;
    }

    @Override
    public DrawableModule getDrawableModule () {
        return drawableModule;
    }

    @Override
    public Vector3 getEffectPosition () {
        return effectPosition;
    }

    @Override
    public ScopePayload getScope () {
        return scopePayload;
    }

    @Override
    public Color getTint () {
        return tint;
    }

    @Override
    public void setScope (ScopePayload scope) {
        this.scopePayload = scope;
    }

    @Override
    public int getActiveParticleCount () {
        return activeParticles.size;
    }

    @Override
    public boolean isContinuous () {
        return isContinuous;
    }

    @Override
    public boolean isComplete () {
        return isComplete;
    }

    public void stop() {
        alpha = 1f;
        isStopped = true;
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    @Override
    public void restart () {
        delayTimer = delay;
        alpha = 0;
        isComplete = false;
        particlesToEmmit = 1f;
        isStopped = false;
    }

    @Override
    public float getDelayRemaining () {
        return delayTimer;
    }

    @Override
    public void update (float delta) {
        seek(cursor + delta);
    }

    public class ParticleRecord {
        public float start;
        public float end;
        public float seed;
    }

    public void seek(float time) {
        if(timeline.size == 0) return; // nothing to do

        int index = timeline.size - 1;
        ParticleRecord last = timeline.get(index);

        float duration = last.start;
        time %= duration;

        if (time < last.start) {
            // need to find where we are, approximate first, then search for fastest result
            int approximation = MathUtils.clamp(Math.round((time/last.start) * timeline.size), 0, timeline.size - 1);
            int found = -1;
            int i = 0;
            if(checkRecordToBeTheSeekPosition(approximation, time)) {
                found = approximation;
            }
            while(found == -1) {
                boolean left = checkRecordToBeTheSeekPosition(approximation - i, time);
                boolean right = checkRecordToBeTheSeekPosition(approximation + i, time);
                if(left) {
                    found = approximation - i;
                }
                if(right) {
                    found = approximation + i;
                }
                i++;
                if(approximation - i < 0 && approximation + i > timeline.size-1) {
                    found = index;
                }
            }
            index = found;
            System.out.println("seek search complete in " + i + " steps");
        }

        // now time to gather particles that have collisions, till first one that does not
        int count = 0;
        float delta = time - cursor;
        cursor = time;
        newMap.clear();
        activeParticles.clear();
        while(index >= 0 && timeline.get(index).end > time) {
            count++;

            ParticleRecord record = timeline.get(index);
            float particleAlpha = (time - record.start) / (record.end - record.start);
            float particleSeed = record.seed;

            Particle particle;
            boolean initCalled = false;
            if (recordMap.containsKey(record)) {
                particle = recordMap.get(record);
            } else {
                particle = particlePool.obtain();
                particle.init(this, particleSeed);
                initCalled = true;
                recordMap.put(record, particle);
            }

            if(Math.abs(delta) > 0.1f) {
                // we need to re-seek this state from left
                if(!initCalled) {
                    particle.init(this);
                }

                // now fast forward
                float pos = 0f;
                float miniDelta = 1/30f;
                int ffCount = 0;
                while(pos < time - record.start) {
                    pos += miniDelta;
                    if (pos > time - record.start) {
                        miniDelta = time - record.start - pos;
                        pos = time - record.start;
                    }
                    particle.update(null, miniDelta);
                    ffCount++;
                }
                System.out.println("fast forwarded in " + ffCount + " steps");
            } else {
                particle.applyAlpha(particleAlpha, delta);
            }

            newMap.put(record, particle);
            activeParticles.add(particle);

            index--;
        }

        tmp.clear();
        for(ParticleRecord oldRecord: recordMap.keys()) {
            if(!newMap.containsKey(oldRecord)) {
                particlePool.free(recordMap.get(oldRecord));
                tmp.add(oldRecord);
            }
        }
        for(ParticleRecord oldRecord : tmp) {
            recordMap.remove(oldRecord);
        }

        System.out.println("found " + count + " collisions");
    }

    private boolean checkRecordToBeTheSeekPosition(int index, float time) {
        if(index < 0 || index >= timeline.size) return false;

        ParticleRecord record =  timeline.get(index);

        if(index >= timeline.size - 1) {
            if (record.start < time) {
                return true;
            } else {
                return false;
            }
        }

        ParticleRecord recordNext =  timeline.get(index + 1);

        if(record.start <= time && recordNext.start >= time) {
            return true;
        }

        return false;
    }

    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }
}
