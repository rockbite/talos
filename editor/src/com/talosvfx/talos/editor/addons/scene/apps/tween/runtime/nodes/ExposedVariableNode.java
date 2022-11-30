package com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.nodes;

import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.RoutineInstance;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.utils.scriptProperties.PropertyWrapper;

public class ExposedVariableNode extends RoutineNode {

    public int index;

    private transient PropertyWrapper<?> propertyWrapper;

    public void updateForPropertyWrapper (PropertyWrapper<?> propertyWrapper) {
        index = propertyWrapper.index;
        this.propertyWrapper = propertyWrapper;
    }
    @Override
    public Object queryValue (String targetPortName) {
        if (propertyWrapper == null) {
            return 0;
        }

        return propertyWrapper.value;
    }

    @Override
    public void loadFrom (RoutineInstance routineInstance, JsonValue nodeData) {
        super.loadFrom(routineInstance, nodeData);
        index = nodeData.getInt("index", 0);
    }
}
