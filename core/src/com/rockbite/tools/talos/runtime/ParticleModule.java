package com.rockbite.tools.talos.runtime;

public class ParticleModule extends Module {


    public static final int ID = 0;
    public static final int TAG = 1;
    public static final int DRAWABLE = 2;
    public static final int OFFSET = 3;
    public static final int POSITION = 4;
    public static final int LIFE = 6;

    public static final int VELOCITY = 7;
    public static final int GRAVITY = 8;
    public static final int ROTATION = 9;
    public static final int TARGET = 10;
    public static final int COLOR = 11;
    public static final int TRANSPARENCY = 12;
    public static final int ANGLE = 13;
    public static final int MASS = 14;
    public static final int SIZE = 15;

    private ScopePayload scopePayload;

    Value tmp = new Value();

    @Override
    public void init(ParticleSystem system) {
        super.init(system);
        scopePayload = new ScopePayload();
        createInputSlots(15);
    }

    @Override
    public void processValues(ScopePayload scopePayload) {

    }

    public void updateScopeData(Particle particle) {
        scopePayload.set(ScopePayload.EMITTER_ALPHA, particle.particleEmitter.alpha);
        scopePayload.set(ScopePayload.PARTICLE_ALPHA, particle.alpha);
        scopePayload.set(ScopePayload.PARTICLE_SEED, particle.seed);
    }

    public float getTransparency() {
        getInputValue(tmp, TRANSPARENCY, scopePayload);

        if(tmp.isDefault) return 1; // defaults

        return tmp.floatVars[0];
    }

    public float getLife() {
        getInputValue(tmp, LIFE, scopePayload);

        if(tmp.isDefault) return 2; // defaults

        return tmp.floatVars[0];
    }

    public float getAngle() {
        getInputValue(tmp, ANGLE, scopePayload);

        if(tmp.isDefault) return 90; // defaults

        return tmp.floatVars[0];
    }

    public float getVelocity() {
        getInputValue(tmp, VELOCITY, scopePayload);

        if(tmp.isDefault) return 1; // defaults

        return tmp.floatVars[0];
    }

    public float getRotation() {
        getInputValue(tmp, ROTATION, scopePayload);

        if(tmp.isDefault) return 0; // defaults

        return tmp.floatVars[0];
    }

    public float getSize() {
        getInputValue(tmp, SIZE, scopePayload);

        if(tmp.isDefault) return 50; // defaults

        return tmp.floatVars[0];
    }
}
