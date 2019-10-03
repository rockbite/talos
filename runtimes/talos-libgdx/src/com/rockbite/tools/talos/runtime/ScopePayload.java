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

    public NumericalValue[] internalMap = new NumericalValue[10];

    public ScopePayload() {
        for(int i = 0; i < internalMap.length; i++) {
            internalMap[i] = new NumericalValue();
        }
    }


    public void reset() {
        for(int i = 0; i < internalMap.length; i++) {
            map.get(i).set(0);
        }
    }
}
