package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.widgets.propertyWidgets.FloatPropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.Vector2PropertyWidget;

public class TransformComponent implements IComponent {
    public Vector2 position = new Vector2();
    public float rotation;
    public Vector2 scale = new Vector2();

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        Vector2PropertyWidget positionWidget = new Vector2PropertyWidget("Position") {
            @Override
            public Vector2 getValue () {
                return position;
            }

            @Override
            public void valueChanged (Vector2 value) {
                position.set(value);
            }
        };

        FloatPropertyWidget rotationWidget = new FloatPropertyWidget("Rotation: ") {
            @Override
            public Float getValue () {
                return rotation;
            }

            @Override
            public void valueChanged (Float value) {
                TransformComponent.this.rotation = value;
            }
        };

        Vector2PropertyWidget scaleWidget = new Vector2PropertyWidget("Scale") {
            @Override
            public Vector2 getValue () {
                return scale;
            }

            @Override
            public void valueChanged (Vector2 value) {
                scale.set(value);
            }
        };

        properties.add(positionWidget);
        properties.add(rotationWidget);
        properties.add(scaleWidget);

        return properties;
    }

    @Override
    public String getPropertyBoxTitle () {
        return "Transform";
    }

    @Override
    public int getPriority () {
        return 1;
    }
}
