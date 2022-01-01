package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpriteMetadata;

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

    private ObjectMap<Texture, NinePatch> patchCache = new ObjectMap<>();

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

            SpriteMetadata metadata = SceneEditorAddon.get().workspace.getMetadata(spriteRenderer.path, SpriteMetadata.class);

            vec.set(0, 0);
            transformComponent.localToWorld(gameObject, vec);
            Vector2 renderPosition = vec;

            if(spriteRenderer.getTexture() != null) {
                batch.setColor(spriteRenderer.color);


                if(metadata.borderData !=null) {
                    Texture texture = spriteRenderer.getTexture().getTexture(); // todo: pelase fix me, i am such a shit
                    NinePatch patch = obtainNinePatch(texture, metadata.borderData);// todo: this has to be done better
                    //todo: and this renders wrong so this needs fixing too
                    patch.draw(batch,
                            renderPosition.x - 0.5f, renderPosition.y - 0.5f,
                            0.5f, 0.5f,
                            transformComponent.scale.x, transformComponent.scale.y,
                            1f, 1f,
                            transformComponent.rotation);
                } else {
                    batch.draw(spriteRenderer.getTexture(),
                            renderPosition.x - 0.5f, renderPosition.y - 0.5f,
                            0.5f, 0.5f,
                            1f, 1f,
                            transformComponent.scale.x, transformComponent.scale.y,
                            transformComponent.rotation);
                }


                batch.setColor(Color.WHITE);
            }
        }
    }

    private NinePatch obtainNinePatch (Texture texture, int[] metadata) {
        if(patchCache.containsKey(texture)) {
            return patchCache.get(texture);
        } else {
            NinePatch patch = new NinePatch(texture, metadata[0], metadata[1], metadata[2], metadata[3]);
            patch.scale(1/100f, 1/100f); // fix this later
            patchCache.put(texture, patch);
            return patch;
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
