package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.utils.IntMap;
import com.rockbite.tools.talos.runtime.values.NumericalValue;
import com.rockbite.tools.talos.runtime.values.Value;

public class ScopePayload {

    public static final int EMITTER_ALPHA = 0;
    public static final int PARTICLE_ALPHA = 1;
    public static final int PARTICLE_SEED = 2;
    public static final int REQUESTER_ID = 3;
    public static final int EMITTER_ALPHA_AT_P_INIT = 4;
    public static final int DRAWABLE_ASPECT_RATIO = 5;

    private IntMap<NumericalValue> map = new IntMap<>();

    public ScopePayload() {
        for(int i = 0; i < 10; i++) {
            map.put(i, new NumericalValue());
        }
    }

    public void set(int index, float value) {
        map.get(index).set(value);
    }

    public void set(int index, NumericalValue value) {
        map.get(index).set(value);
    }

    public NumericalValue get(int index) {
        return map.get(index);
    }

    public float getFloat(int index) {
        return map.get(index).getFloat();
    }

    public void reset() {
        for(int i = 0; i < 10; i++) {
            map.get(i).set(0);
        }
    }
}