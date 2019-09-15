package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.rockbite.tools.talos.runtime.Particle;
import com.rockbite.tools.talos.runtime.ParticleSystem;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.values.ColorValue;
import com.rockbite.tools.talos.runtime.values.FloatValue;
import com.rockbite.tools.talos.runtime.values.Vector2Value;

public class ParticleModule extends Module {


    public static final int ID = 0;
    public static final int TAG = 1;
    public static final int DRAWABLE = 2;
    public static final int OFFSET = 3;
    public static final int LIFE = 4;

    public static final int VELOCITY = 5;
    public static final int GRAVITY = 6;
    public static final int ROTATION = 7;
    public static final int TARGET = 8;
    public static final int COLOR = 9;
    public static final int TRANSPARENCY = 10;
    public static final int ANGLE = 11;
    public static final int MASS = 12;
    public static final int SIZE = 13;

    private ScopePayload scopePayload;

    Vector2 vec2 = new Vector2();

    @Override
    public void init(ParticleSystem system) {
        super.init(system);
        scopePayload = new ScopePayload();

        createInputSlots(15);
        inputValues.put(COLOR, new ColorValue());
        inputValues.put(OFFSET, new Vector2Value());
        inputValues.put(TARGET, new Vector2Value());
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
        getInputValue(TRANSPARENCY, scopePayload);

        if(getInput(TRANSPARENCY).isEmpty()) return 1; // defaults

        return (float) getInput(TRANSPARENCY).get();
    }

    public float getLife() {
        getInputValue(LIFE, scopePayload);

        if(getInput(LIFE).isEmpty()) return 2; // defaults

        return (float) getInput(LIFE).get();
    }

    public float getAngle() {
        getInputValue(ANGLE, scopePayload);

        if(getInput(ANGLE).isEmpty()) return 90; // defaults

        return (float) getInput(ANGLE).get();
    }

    public float getVelocity() {
        getInputValue(VELOCITY, scopePayload);

        if(getInput(VELOCITY).isEmpty()) return 1; // defaults

        return (float) getInput(VELOCITY).get();
    }

    public float getRotation() {
        getInputValue(ROTATION, scopePayload);

        if(getInput(ROTATION).isEmpty()) return 0; // defaults

        return (float) getInput(ROTATION).get();
    }

    public float getSize() {
        getInputValue(SIZE, scopePayload);

        if(getInput(SIZE).isEmpty()) return 50; // defaults

        return (float) getInput(SIZE).get();
    }

    public Color getColor() {
        getInputValue(COLOR, scopePayload);

        if(inputValues.get(COLOR).isEmpty()) return Color.WHITE; // defaults

        return (Color) inputValues.get(COLOR).get();
    }

    public Vector2 getStartPosition() {
        getInputValue(OFFSET, scopePayload);

        if(inputValues.get(OFFSET).isEmpty()) {
            vec2.set(0, 0);
            return vec2; // defaults
        }

        return (Vector2) inputValues.get(OFFSET).get();
    }

    public Vector2 getTarget() {
        getInputValue(TARGET, scopePayload);

        if(inputValues.get(TARGET).isEmpty()) {
            return null; // defaults
        }

        return (Vector2) inputValues.get(TARGET).get();
    }
}
