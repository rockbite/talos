package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.scene.GameObject;import com.talosvfx.talos.editor.widgets.propertyWidgets.*;

public class TransformComponent extends AComponent {
    @ValueProperty(prefix = {"X", "Y"})
    public Vector2 position = new Vector2();

    @ValueProperty(min = -360, max = 360, step=0.5f, progress = true)
    public float rotation;

    @ValueProperty(prefix = {"W", "H"})
    public Vector2 scale = new Vector2(1, 1);

    public transient Vector2 pivot = new Vector2(0.5f, 0.5f);

    public transient Vector2 worldPosition = new Vector2();
    public transient Vector2 worldScale = new Vector2(1, 1);
    public transient float worldRotation = 0;

    public static Array<GameObject> tmp = new Array<>();
    public static Vector2 vec = new Vector2();

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        PropertyWidget positionWidget = WidgetFactory.generate(this, "position", "Position");
        PropertyWidget rotationWidget = WidgetFactory.generate(this, "rotation", "Rotation");
        PropertyWidget scaleWidget = WidgetFactory.generate(this, "scale", "Scale");

        properties.add(positionWidget);
        properties.add(rotationWidget);
        properties.add(scaleWidget);

        return properties;
    }

    @Override
    public Class<? extends IPropertyProvider> getType() {
        return getClass();
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

            vector.scl(transform.scale);
            vector.rotateDeg(transform.rotation);

            vector.add(transform.position);
            vector.add(gameObject.getTransformSettings().offsetX, gameObject.getTransformSettings().offsetY);


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


                vector.sub(transform.position);
                vector.rotateDeg(-transform.rotation);
                vector.scl(1f/transform.scale.x, 1f/transform.scale.y);
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

    @Override
    public void reset() {
        super.reset();
        position.setZero();
        rotation = 0;
        scale.setZero();
    }
}
