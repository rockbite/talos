package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.rockbite.tools.talos.runtime.Particle;
import com.rockbite.tools.talos.runtime.ParticleSystem;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

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


    NumericalValue offset;
    NumericalValue life;
    NumericalValue velocity;
    NumericalValue gravity;
    NumericalValue rotation;
    NumericalValue target;
    NumericalValue color;
    NumericalValue transparency;
    NumericalValue angle;
    NumericalValue mass;
    NumericalValue size;

    Color tmpColor = new Color();
    Vector2 tmpVec = new Vector2();

    @Override
    protected void defineSlots() {
        offset = createInputSlot(OFFSET);
        life = createInputSlot(LIFE);
        velocity = createInputSlot(VELOCITY);
        gravity = createInputSlot(GRAVITY);
        rotation = createInputSlot(ROTATION);
        target = createInputSlot(TARGET);
        color = createInputSlot(COLOR);
        transparency = createInputSlot(TRANSPARENCY);
        angle = createInputSlot(ANGLE);
        mass = createInputSlot(MASS);
        size = createInputSlot(SIZE);
    }

    @Override
    public void processValues() {
        // nothing to process, it's all cool as cucumber
    }

    public void updateScopeData(Particle particle) {
        getScope().set(ScopePayload.EMITTER_ALPHA, particle.particleEmitter.alpha);
        getScope().set(ScopePayload.PARTICLE_ALPHA, particle.alpha);
        getScope().set(ScopePayload.PARTICLE_SEED, particle.seed);
        getScope().set(ScopePayload.REQUESTER_ID, particle.seed);
        getScope().set(ScopePayload.EMITTER_ALPHA_AT_P_INIT, particle.durationAtInit);
    }

    public float getTransparency() {
        fetchInputSlotValue(TRANSPARENCY);
        if(transparency.isEmpty()) return 1; // defaults
        return transparency.getFloat();
    }

    public float getLife() {
        fetchInputSlotValue(LIFE);
        if(life.isEmpty()) return 2; // defaults
        return life.getFloat();
    }

    public float getAngle() {
        fetchInputSlotValue(ANGLE);
        if(angle.isEmpty()) return 90; // defaults
        return angle.getFloat();
    }

    public float getVelocity() {
        fetchInputSlotValue(VELOCITY);
        if(velocity.isEmpty()) return 100; // defaults
        return velocity.getFloat();
    }

    public float getRotation() {
        fetchInputSlotValue(ROTATION);
        if(rotation.isEmpty()) return 0; // defaults
        return rotation.getFloat();
    }

    public float getSize() {
        fetchInputSlotValue(SIZE);
        if(size.isEmpty()) return 50; // defaults
        return size.getFloat();
    }

    public Color getColor() {
        fetchInputSlotValue(COLOR);
        if(color.isEmpty()) return Color.WHITE; // defaults
        tmpColor.set(color.get(0), color.get(1), color.get(2), 1f);
        return tmpColor;
    }

    public Vector2 getStartPosition() {
        fetchInputSlotValue(OFFSET);
        if(offset.isEmpty()) {
            tmpVec.set(0, 0);
            return tmpVec;
        }
        tmpVec.set(offset.get(0), offset.get(1));

        return tmpVec;
    }

    public Vector2 getTarget() {
        fetchInputSlotValue(TARGET);
        if(target.isEmpty()) {
            return null;
        }
        tmpVec.set(target.get(0), target.get(1));

        return tmpVec;
    }
}
