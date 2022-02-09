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

package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ScopePayload;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class ParticleModule extends AbstractModule {

    public static final int ID = 0;
    public static final int TAG = 1;

    public static final int SPAWN_POSITION = 25;

    public static final int INITIAL_VELOCITY = 17;
    public static final int VELOCITY_OVER_TIME = 18;

    public static final int INITIAL_SPIN_VELOCITY = 19;
    public static final int SPIN_OVER_TIME = 20;

    public static final int FORCES = 21;
    public static final int GRAVITY = 22;
    public static final int DRAG = 26;
    public static final int POSITION_OVERRIDE = 27;
    public static final int ROTATION_OVERRIDE = 28;

    public static final int LIFE = 4;
    public static final int COLOR = 9;
    public static final int TRANSPARENCY = 10;

    public static final int PIVOT = 15;

    NumericalValue life;
    NumericalValue color;
    NumericalValue transparency;

    NumericalValue spawnPosition;
    NumericalValue initialVelocity;
    NumericalValue velocityOverTime;
    NumericalValue initialSpinVelocity;
    NumericalValue spinVelocityOverTime;
    NumericalValue forces;
    NumericalValue gravity;
    NumericalValue drag;

    NumericalValue positionOverride;
    NumericalValue rotationOverride;

    NumericalValue pivot;

    Color tmpColor = new Color();
    Vector2 tmpVec = new Vector2();
    Vector3 tmp3Vec = new Vector3();


    @Override
    protected void defineSlots() {
        life = createInputSlot(LIFE);

        spawnPosition = createInputSlot(SPAWN_POSITION);

        positionOverride = createInputSlot(POSITION_OVERRIDE);
        rotationOverride = createInputSlot(ROTATION_OVERRIDE);

        initialVelocity = createInputSlot(INITIAL_VELOCITY);
        velocityOverTime = createInputSlot(VELOCITY_OVER_TIME);

        initialSpinVelocity = createInputSlot(INITIAL_SPIN_VELOCITY);
        spinVelocityOverTime = createInputSlot(SPIN_OVER_TIME);

        forces = createInputSlot(FORCES);
        gravity = createInputSlot(GRAVITY);
        drag = createInputSlot(DRAG);



        color = createInputSlot(COLOR);
        transparency = createInputSlot(TRANSPARENCY);

        pivot = createInputSlot(PIVOT);
    }

    @Override
    public void processCustomValues () {
        // nothing to process, it's all cool as cucumber
    }

    public void updateScopeData(Particle particle) {
        getScope().set(ScopePayload.EMITTER_ALPHA, particle.getEmitterAlpha());
        getScope().set(ScopePayload.PARTICLE_ALPHA, particle.alpha);
        getScope().set(ScopePayload.PARTICLE_SEED, particle.seed);
        getScope().set(ScopePayload.EMITTER_ALPHA_AT_P_INIT, particle.durationAtInit);
        getScope().set(ScopePayload.PARTICLE_POSITION, particle.getX(), particle.getY(), particle.getZ());

        getScope().setCurrentRequesterID(particle.requesterID);

        getScope().setParticle(particle);
    }

    public float getTransparency() {
        fetchInputSlotValue(TRANSPARENCY);
        if(transparency.isEmpty()) return 1; // defaults
        return transparency.getFloat();
    }

    public Vector3 getInitialVelocity () {
        fetchInputSlotValue(INITIAL_VELOCITY);
        if (initialVelocity.isEmpty()) {
            return tmp3Vec.setZero();
        }
        return tmp3Vec.set(initialVelocity.get(0), initialVelocity.get(1), initialVelocity.get(2));
    }

    public Vector3 getInitialSpinVelocity () {
        fetchInputSlotValue(INITIAL_SPIN_VELOCITY);
        if (initialSpinVelocity.isEmpty()) {
            return tmp3Vec.setZero();
        }
        return tmp3Vec.set(initialSpinVelocity.get(0), initialSpinVelocity.get(1), initialSpinVelocity.get(2));
    }

    public Vector3 getForces () {
        fetchInputSlotValue(FORCES);
        if (forces.isEmpty()) {
            return tmp3Vec.setZero();
        }
        return tmp3Vec.set(forces.get(0), forces.get(1), forces.get(2));
    }


    public boolean hasDrag () {
        return !drag.isEmpty();
    }

    public Vector3 getDrag () {
        fetchInputSlotValue(DRAG);
        if (drag.isEmpty()) {
            return tmp3Vec.setZero();
        }
        return tmp3Vec.set(drag.get(0), drag.get(1), drag.get(2));
    }

    public Vector3 getGravity () {
        fetchInputSlotValue(GRAVITY);
        if (gravity.isEmpty()) {
            return tmp3Vec.setZero();
        }
        return tmp3Vec.set(gravity.get(0), gravity.get(1), gravity.get(2));
    }

    public Vector3 getSpawnPosition () {
        fetchInputSlotValue(SPAWN_POSITION);
        if (spawnPosition.isEmpty()) {
            return tmp3Vec.setZero();
        }
        return tmp3Vec.set(spawnPosition.get(0), spawnPosition.get(1), spawnPosition.get(2));
    }

    public Vector3 getVelocityOverTime () {
        fetchInputSlotValue(VELOCITY_OVER_TIME);
        if (velocityOverTime.isEmpty()) {
            return tmp3Vec.setZero();
        }
        return tmp3Vec.set(velocityOverTime.get(0), velocityOverTime.get(1), velocityOverTime.get(2));
    }

    public Vector3 getSpinVelocityOverTime () {
        fetchInputSlotValue(SPIN_OVER_TIME);
        if (spinVelocityOverTime.isEmpty()) {
            return tmp3Vec.setZero();
        }
        return tmp3Vec.set(spinVelocityOverTime.get(0), spinVelocityOverTime.get(1), spinVelocityOverTime.get(2));
    }

    public boolean hasVelocityOverTime () {
        fetchInputSlotValue(VELOCITY_OVER_TIME);
        return !velocityOverTime.isEmpty();
    }

    public boolean hasSpinVelocityOverTime () {
        fetchInputSlotValue(SPIN_OVER_TIME);
        return !spinVelocityOverTime.isEmpty();
    }

    public float getLife() {
        fetchInputSlotValue(LIFE);
        if(life.isEmpty()) return 1; // defaults
        return life.getFloat();
    }

    public boolean hasPositionOverride () {
        fetchInputSlotValue(POSITION_OVERRIDE);
        return !positionOverride.isEmpty();
    }

    public Vector3 getPositionOverride () {
        fetchInputSlotValue(POSITION_OVERRIDE);
        if (positionOverride.isEmpty()) {
            return tmp3Vec.setZero();
        }
        return tmp3Vec.set(positionOverride.get(0), positionOverride.get(1), positionOverride.get(2));
    }

    public boolean hasRotationOverride () {
        fetchInputSlotValue(ROTATION_OVERRIDE);
        return !rotationOverride.isEmpty();
    }

    public Vector3 getRotationOverride () {
        fetchInputSlotValue(ROTATION_OVERRIDE);
        if (rotationOverride.isEmpty()) {
            return tmp3Vec.setZero();
        }
        return tmp3Vec.set(rotationOverride.get(0), rotationOverride.get(1), rotationOverride.get(2));
    }

    /**
     * allowed values are from 0 to 1 where 0.5 is default center
     * @return
     */
    public Vector2 getPivot() {
        fetchInputSlotValue(PIVOT);
        if(pivot.isEmpty()) {
            pivot.set(0.5f, 0.5f);
        }
        tmpVec.set(pivot.get(0), pivot.get(1));

        if(tmpVec.x > 1f) tmpVec.x = 1f;
        if(tmpVec.y> 1f) tmpVec.y = 1f;
        if(tmpVec.x < 0f) tmpVec.x = 0f;
        if(tmpVec.y < 0f) tmpVec.y = 0f;

        return tmpVec;
    }

    public Color getColor() {
        fetchInputSlotValue(COLOR);
        if(color.isEmpty()) return Color.WHITE; // defaults
        tmpColor.set(color.get(0), color.get(1), color.get(2), 1f);
        return tmpColor;
    }


    @Override
    public void write (Json json) {
        super.write(json);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
    }


}
