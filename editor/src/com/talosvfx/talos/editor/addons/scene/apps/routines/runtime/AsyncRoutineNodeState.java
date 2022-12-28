package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime;

import com.badlogic.gdx.utils.Pool;
import lombok.Setter;

public class AsyncRoutineNodeState<T> implements Pool.Poolable {
    public float alpha;
    @Setter
    T target;

    @Override
    public void reset() {
        alpha = 0;
    }
}
