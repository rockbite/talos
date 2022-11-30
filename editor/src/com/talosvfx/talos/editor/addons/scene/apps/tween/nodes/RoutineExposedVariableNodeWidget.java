package com.talosvfx.talos.editor.addons.scene.apps.tween.nodes;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.talosvfx.talos.editor.addons.scene.utils.scriptProperties.PropertyWrapper;
import com.talosvfx.talos.editor.nodes.widgets.TextValueWidget;

public class RoutineExposedVariableNodeWidget extends RoutineNodeWidget {

    public transient int index;

    public void update(PropertyWrapper<?> propertyWrapper) {
        TextValueWidget textValueWidget = (TextValueWidget) getWidget("key");
        textValueWidget.setTouchable(Touchable.disabled);

        if (propertyWrapper != null) {
            textValueWidget.setValue(propertyWrapper.propertyName);
            index = propertyWrapper.index;
        } else {
            textValueWidget.setValue("-");
            index = 0;
        }
    }
}
