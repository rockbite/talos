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

    FloatValue tmp = new FloatValue();
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
        getInputValue(tmp, TRANSPARENCY, scopePayload);

        if(tmp.isEmpty()) return 1; // defaults

        return tmp.get();
    }

    public float getLife() {
        getInputValue(tmp, LIFE, scopePayload);

        if(tmp.isEmpty()) return 2; // defaults

        return tmp.get();
    }

    public float getAngle() {
        getInputValue(tmp, ANGLE, scopePayload);

        if(tmp.isEmpty()) return 90; // defaults

        return tmp.get();
    }

    public float getVelocity() {
        getInputValue(tmp, VELOCITY, scopePayload);

        if(tmp.isEmpty()) return 1; // defaults

        return tmp.get();
    }

    public float getRotation() {
        getInputValue(tmp, ROTATION, scopePayload);

        if(tmp.isEmpty()) return 0; // defaults

        return tmp.get();
    }

    public float getSize() {
        getInputValue(tmp, SIZE, scopePayload);

        if(tmp.isEmpty()) return 50; // defaults

        return tmp.get();
    }

    public Color getColor() {
        getInputValue(inputValues.get(COLOR), COLOR, scopePayload);

        if(inputValues.get(COLOR).isEmpty()) return Color.WHITE; // defaults

        return (Color) inputValues.get(COLOR).get();
    }

    public Vector2 getStartPosition() {
        getInputValue(inputValues.get(OFFSET), OFFSET, scopePayload);

        if(inputValues.get(OFFSET).isEmpty()) {
            vec2.set(0, 0);
            return vec2; // defaults
        }

        return (Vector2) inputValues.get(OFFSET).get();
    }

    public Vector2 getTarget() {
        getInputValue(inputValues.get(TARGET), TARGET, scopePayload);

        if(inputValues.get(TARGET).isEmpty()) {
            return null; // defaults
        }

        return (Vector2) inputValues.get(TARGET).get();
    }
}
