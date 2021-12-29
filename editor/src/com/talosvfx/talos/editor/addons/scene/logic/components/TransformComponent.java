package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.widgets.propertyWidgets.FloatPropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.Vector2PropertyWidget;

public class TransformComponent implements IComponent {
    public Vector2 position = new Vector2();
    public float rotation;
    public Vector2 scale = new Vector2(1, 1);

    public static Array<GameObject> tmp = new Array<>();

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

        FloatPropertyWidget rotationWidget = new FloatPropertyWidget("Rotation") {
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

    public static Vector2 localToWorld(GameObject gameObject, Vector2 vector) {
        if(gameObject.hasComponent(TransformComponent.class)) {
            TransformComponent transform = gameObject.getComponent(TransformComponent.class);

            vector.add(transform.position);
            vector.rotateDeg(transform.rotation);
            vector.scl(transform.scale);

            if(gameObject.parent != null) {
                localToWorld(gameObject.parent, vector);
            }
        }

        return vector;
    }

    public static Vector2 worldToLocal(GameObject gameObject, Vector2 vector) {
        if(gameObject.parent == null) return vector;

        tmp.clear();
        tmp = getRootChain(gameObject, tmp);

        for(int i = tmp.size - 1; i >= 0; i--) {
            GameObject item = tmp.get(i);
            if(item.hasComponent(TransformComponent.class)) {
                TransformComponent transform = item.getComponent(TransformComponent.class);

                vector.scl(1f/transform.scale.x, 1f/transform.scale.y);
                vector.rotateDeg(-transform.rotation);
                vector.sub(transform.position);
            }
        }

        return vector;
    }

    private static Array<GameObject> getRootChain(GameObject currObject, Array<GameObject> chain) {
        chain.add(currObject);

        if(currObject.parent != null) {
            return getRootChain(currObject.parent, chain);
        }

        return chain;
    }
}
