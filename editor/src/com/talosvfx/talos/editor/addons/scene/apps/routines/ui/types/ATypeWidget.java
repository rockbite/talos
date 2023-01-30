package com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

public abstract class ATypeWidget<T> extends Table {

    public abstract String getTypeName();

    public abstract boolean isFastChange();

    protected boolean fireChangedEvent() {
        ChangeListener.ChangeEvent changeEvent = Pools.obtain(ChangeListener.ChangeEvent.class);

        boolean var2 = false;
        try {
            var2 = fire(changeEvent);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Pools.free(changeEvent);
        }

        return var2;
    }

    public abstract void updateFromPropertyWrapper(PropertyWrapper<T> propertyWrapper);

    public abstract void applyValueToWrapper(PropertyWrapper<T> propertyWrapper);
}
