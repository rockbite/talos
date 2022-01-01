package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.ParticleComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.RendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpriteMetadata;
import com.talosvfx.talos.runtime.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.ParticleEffectInstance;
import com.talosvfx.talos.runtime.render.SpriteBatchParticleRenderer;

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
    private ObjectMap<GameObject, ParticleEffectInstance> particleCache = new ObjectMap<>();

    private SpriteBatchParticleRenderer talosRenderer;

    public MainRenderer() {
        for (int i = 0; i < 4; i++) {
            points[i] = new Vector2();
        }

        talosRenderer = new SpriteBatchParticleRenderer();

        layerComparator = new Comparator<GameObject>() {
            @Override
            public int compare (GameObject o1, GameObject o2) {

                RendererComponent o1c = o1.getComponentSlow(RendererComponent.class);
                RendererComponent o2c = o2.getComponentSlow(RendererComponent.class);
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
        list = root.getChildrenByComponent(RendererComponent.class, list);
        sort(list);

        for(GameObject gameObject: list) {
            TransformComponent transformComponent = getWorldTransform(gameObject);

            if(gameObject.hasComponent(SpriteRendererComponent.class)) {
                renderSprite(batch, gameObject);
            } else if(gameObject.hasComponent(ParticleComponent.class)) {
                renderParticle(batch, gameObject);
            }
        }
    }

    private void renderParticle (Batch batch, GameObject gameObject) {
        ParticleComponent particleComponent = gameObject.getComponent(ParticleComponent.class);

        vec.set(0, 0);
        transformComponent.localToWorld(gameObject, vec);
        Vector2 renderPosition = vec;

        if(particleComponent.descriptor == null) {
            particleComponent.reloadDescriptor();
        }

        if(particleComponent.descriptor != null) {
            ParticleEffectInstance instance = obtainParticle(gameObject, particleComponent.descriptor);
            instance.setPosition(renderPosition.x, renderPosition.y);
            instance.update(Gdx.graphics.getDeltaTime()); // todo: we so hacky hacky
            talosRenderer.setBatch(batch);
            talosRenderer.render(instance);
        }
    }

    private void renderSprite (Batch batch, GameObject gameObject) {
        SpriteRendererComponent spriteRenderer = gameObject.getComponent(SpriteRendererComponent.class);
        SpriteMetadata metadata = SceneEditorAddon.get().workspace.getMetadata(spriteRenderer.path, SpriteMetadata.class);
        vec.set(0, 0);
        transformComponent.localToWorld(gameObject, vec);
        Vector2 renderPosition = vec;

        if(spriteRenderer.getTexture() != null) {
            batch.setColor(spriteRenderer.color);


            if(metadata != null && metadata.borderData !=null && spriteRenderer.renderMode == SpriteRendererComponent.RenderMode.sliced) {
                Texture texture = spriteRenderer.getTexture().getTexture(); // todo: pelase fix me, i am such a shit
                NinePatch patch = obtainNinePatch(texture, metadata.borderData);// todo: this has to be done better
                //todo: and this renders wrong so this needs fixing too
                patch.draw(batch,
                        renderPosition.x - 0.5f * transformComponent.scale.x, renderPosition.y - 0.5f * transformComponent.scale.y,
                        0.5f * transformComponent.scale.x, 0.5f * transformComponent.scale.y,
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

    private ParticleEffectInstance obtainParticle (GameObject gameObject, ParticleEffectDescriptor descriptor) {
        if(particleCache.containsKey(gameObject)) {
            return particleCache.get(gameObject);
        } else {
            ParticleEffectInstance instance = descriptor.createEffectInstance();
            particleCache.put(gameObject, instance);
            return instance;
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
