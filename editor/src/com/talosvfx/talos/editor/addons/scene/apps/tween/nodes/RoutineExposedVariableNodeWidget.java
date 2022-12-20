package com.talosvfx.talos.editor.addons.scene.apps.tween.nodes;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.addons.scene.utils.scriptProperties.PropertyWrapper;
import com.talosvfx.talos.editor.nodes.widgets.TextValueWidget;

public class RoutineExposedVariableNodeWidget extends RoutineNodeWidget {

    public int index;

    public void update(PropertyWrapper<?> propertyWrapper) {
        TextValueWidget textValueWidget = (TextValueWidget) getWidget("key");

        if (propertyWrapper != null) {
            textValueWidget.setValue(propertyWrapper.propertyName);
            index = propertyWrapper.index;
        } else {
            textValueWidget.setValue("-");
            index = 0;
        }
    }


    @Override
    protected void writeProperties (Json json) {
        super.writeProperties(json);
        json.writeValue("index", index);
    }

    @Override
    protected void readProperties (JsonValue properties) {
        super.readProperties(properties);
        index = properties.getInt("index", index);
    }

    @Override
    public void constructNode (XmlReader.Element module) {
        super.constructNode(module);
        TextValueWidget textValueWidget = (TextValueWidget) getWidget("key");
        textValueWidget.setTouchable(Touchable.disabled);
    }
}
