package com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers.PropertyWrapper;

public abstract class ATypeWidget extends Table {

    public abstract String getTypeName();


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

    public abstract void applyValueToWrapper(PropertyWrapper<?> propertyWrapper);
}
