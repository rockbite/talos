package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime;

import com.badlogic.gdx.utils.Pool;
import lombok.Getter;
import lombok.Setter;

public class AsyncRoutineNodeState<T> implements Pool.Poolable {
    public float alpha;
    @Setter@Getter
    T target;

    @Setter@Getter
    private float duration;

    @Override
    public void reset() {
        alpha = 0;
    }
}
