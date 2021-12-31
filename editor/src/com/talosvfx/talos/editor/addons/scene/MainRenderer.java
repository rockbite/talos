package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.RendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;

import java.util.Comparator;

public class MainRenderer {

    private final Comparator<GameObject> layerComparator;
    private TransformComponent transformComponent = new TransformComponent();
    private Vector2 vec = new Vector2();
    private Vector2[] points = new Vector2[4];

    private Array<GameObject> list = new Array<>();
    private ObjectMap<String, Integer> layerOrderLookup = new ObjectMap<>();

    private static final int LB = 0;
    private static final int LT = 1;
    private static final int RT = 2;
    private static final int RB = 3;

    public MainRenderer() {
        for (int i = 0; i < 4; i++) {
            points[i] = new Vector2();
        }

        layerComparator = new Comparator<GameObject>() {
            @Override
            public int compare (GameObject o1, GameObject o2) {

                SpriteRendererComponent o1c = o1.getComponent(SpriteRendererComponent.class);
                SpriteRendererComponent o2c = o2.getComponent(SpriteRendererComponent.class);
                int o1l = layerOrderLookup.get(o1c.sortingLayer);
                int o2l = layerOrderLookup.get(o2c.sortingLayer);
                int o1i = o1c.orderingInLayer;
                int o2i = o2c.orderingInLayer;

                if(o1l < o2l) {
                    return -1;
                }
                if(o1l > o2l) {
                    return 1;
                }
                if(o1l == o2l) {
                    if(o1i < o2i) {
                        return -1;
                    }
                    if(o1i > o2i) {
                        return 1;
                    }
                }

                return 0;
            }
        };
    }

    // todo: do fancier logic later
    public void render (Batch batch, GameObject root) {
        updateLayerOrderLookup(root);
        list.clear();
        list = root.getChildrenByComponent(SpriteRendererComponent.class, list);
        sort(list);

        for(GameObject gameObject: list) {
            SpriteRendererComponent spriteRenderer = gameObject.getComponent(SpriteRendererComponent.class);
            TransformComponent transformComponent = getWorldTransform(gameObject);

            vec.set(0, 0);
            transformComponent.localToWorld(gameObject, vec);
            Vector2 renderPosition = vec;

            if(spriteRenderer.getTexture() != null) {
                batch.setColor(spriteRenderer.color);
                batch.draw(spriteRenderer.getTexture(),
                        renderPosition.x - 0.5f, renderPosition.y - 0.5f,
                        0.5f, 0.5f,
                        1f, 1f,
                        transformComponent.scale.x, transformComponent.scale.y,
                        transformComponent.rotation);
                batch.setColor(Color.WHITE);
            }
        }
    }

    private void updateLayerOrderLookup (GameObject root) {
        Array<String> layerList = SceneEditorAddon.get().workspace.getLayerList();
        layerOrderLookup.clear();
        int i = 0;
        for(String layer: layerList) {
            layerOrderLookup.put(layer, i++);
        }
    }

    private void sort (Array<GameObject> list) {
        list.sort(layerComparator);
    }

    private TransformComponent getWorldTransform(GameObject gameObject) {
        getWorldLocAround(gameObject, points[LB], -0.5f, -0.5f);
        getWorldLocAround(gameObject, points[LT],-0.5f, 0.5f);
        getWorldLocAround(gameObject, points[RT],0.5f, 0.5f);
        getWorldLocAround(gameObject, points[RB],0.5f, -0.5f);

        vec.set(points[RT]).sub(points[LB]).scl(0.5f).add(points[LB]); // midpoint
        transformComponent.position.set(vec);
        vec.set(points[RT]).sub(points[LB]);
        transformComponent.scale.set(points[RT].dst(points[LT]), points[RT].dst(points[RB]));
        vec.set(points[RT]).sub(points[LT]).angleDeg();
        transformComponent.rotation = vec.angleDeg();

        return transformComponent;
    }

    private Vector2 getWorldLocAround(GameObject gameObject, Vector2 point, float x, float y) {
        point.set(x, y);
        TransformComponent.localToWorld(gameObject, point);

        return point;
    }
}
