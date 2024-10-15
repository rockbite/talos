package com.talosvfx.talos.runtime.scene.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.ValueProperty;

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
    public static Vector2 localToWorld(GameObject gameObject, Vector2 vector) {
        //gameObject is null so we dont do anything
        if (gameObject == null) return vector;

        if(gameObject.hasTransformComponent()) {
            TransformComponent transform = gameObject.getTransformComponent();

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
        if(gameObject == null) {
            return vector;
        }
        if(gameObject.parent == null) {

            //Check if root has transform component
            if (gameObject.hasTransformComponent()) {
                TransformComponent transform = gameObject.getTransformComponent();

                untransformVectorByTransform(vector, transform);
            }

            return vector;
        }

        tmp.clear();
        tmp = getRootChain(gameObject, tmp);

        for(int i = tmp.size - 1; i >= 0; i--) {
            GameObject item = tmp.get(i);
            if(item.hasTransformComponent()) {
                TransformComponent transform = item.getTransformComponent();

                untransformVectorByTransform(vector, transform);
            }
        }

        return vector;
    }

    private static void untransformVectorByTransform (Vector2 vector, TransformComponent transform) {
        vector.sub(transform.position);
        vector.rotateDeg(-transform.rotation);
        vector.scl(1f/ transform.scale.x, 1f/ transform.scale.y);
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
