package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.RawAsset;
import com.talosvfx.talos.editor.addons.scene.events.ComponentUpdated;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.*;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpineMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpriteMetadata;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.runtime.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.ParticleEffectInstance;
import com.talosvfx.talos.runtime.render.SpriteBatchParticleRenderer;

import java.util.Comparator;

public class MainRenderer implements Notifications.Observer {

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
    private ObjectMap<ParticleComponent, ParticleEffectInstance> particleCache = new ObjectMap<>();

    private SpriteBatchParticleRenderer talosRenderer;
    private SkeletonRenderer spineRenderer;

    private TextureRegion textureRegion = new TextureRegion();

    public MainRenderer() {
        for (int i = 0; i < 4; i++) {
            points[i] = new Vector2();
        }

        Notifications.registerObserver(this);

        talosRenderer = new SpriteBatchParticleRenderer();
        spineRenderer = new SkeletonRenderer();

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

            GameResourceOwner<?> resourceComponent = gameObject.getRenderResourceComponent();
            if (resourceComponent != null) {
                //Its something with a game resource

                GameAsset<?> gameResource = resourceComponent.getGameResource();
                if (gameResource == null || gameResource.isBroken()) {
                    //Render the broken sprite

                    renderBrokenComponent(batch, gameObject, transformComponent);
                    continue;
                }
            }

            if(gameObject.hasComponent(SpriteRendererComponent.class)) {
                renderSprite(batch, gameObject);
            } else if(gameObject.hasComponent(ParticleComponent.class)) {
                renderParticle(batch, gameObject);
            } else if(gameObject.hasComponent(SpineRendererComponent.class)) {
                renderSpine(batch, gameObject);
            }
        }
    }

    private void renderBrokenComponent (Batch batch, GameObject gameObject, TransformComponent transformComponent) {

        vec.set(0, 0);
        transformComponent.localToWorld(gameObject, vec);
        Vector2 renderPosition = vec;

        batch.draw(AssetRepository.getInstance().brokenTextureRegion,
                renderPosition.x - 0.5f, renderPosition.y - 0.5f,
                0.5f, 0.5f,
                1f, 1f,
                transformComponent.scale.x, transformComponent.scale.y,
                transformComponent.rotation);
    }

    private void renderSpine (Batch batch, GameObject gameObject) {

        SpineRendererComponent spineRendererComponent = gameObject.getComponent(SpineRendererComponent.class);

        vec.set(0, 0);
        transformComponent.localToWorld(gameObject, vec);
        Vector2 renderPosition = vec;

        spineRendererComponent.skeleton.setPosition(renderPosition.x, renderPosition.y);
        spineRendererComponent.animationState.update(Gdx.graphics.getDeltaTime());
        spineRendererComponent.animationState.apply(spineRendererComponent.skeleton);
        spineRendererComponent.skeleton.updateWorldTransform();

        spineRenderer.draw(batch, spineRendererComponent.skeleton);
    }

    private void renderParticle (Batch batch, GameObject gameObject) {
        ParticleComponent particleComponent = gameObject.getComponent(ParticleComponent.class);

        vec.set(0, 0);
        transformComponent.localToWorld(gameObject, vec);
        Vector2 renderPosition = vec;

        ParticleEffectInstance instance = obtainParticle(gameObject, particleComponent.gameAsset.getResource());
        instance.setPosition(renderPosition.x, renderPosition.y);
        instance.update(Gdx.graphics.getDeltaTime()); // todo: we so hacky hacky
        talosRenderer.setBatch(batch);
        talosRenderer.render(instance);
    }

    private void renderSprite (Batch batch, GameObject gameObject) {
        SpriteRendererComponent spriteRenderer = gameObject.getComponent(SpriteRendererComponent.class);
        GameAsset<Texture> gameResource = spriteRenderer.getGameResource();
        RawAsset rootRawAsset = gameResource.getRootRawAsset();
        AMetadata metaData = rootRawAsset.metaData;
        if (metaData instanceof SpriteMetadata) {
            //It should be
            SpriteMetadata metadata = (SpriteMetadata)metaData;

            vec.set(0, 0);
            transformComponent.localToWorld(gameObject, vec);
            Vector2 renderPosition = vec;

            Texture resource = spriteRenderer.getGameResource().getResource();
            textureRegion.setRegion(resource);
            if(textureRegion != null) {
                batch.setColor(spriteRenderer.color);

                final float width = spriteRenderer.size.x;
                final float height = spriteRenderer.size.y;

                if(metadata != null && metadata.borderData != null && spriteRenderer.renderMode == SpriteRendererComponent.RenderMode.sliced) {
                    Texture texture = textureRegion.getTexture(); // todo: pelase fix me, i am such a shit
                    NinePatch patch = obtainNinePatch(texture, metadata);// todo: this has to be done better
                    //todo: and this renders wrong so this needs fixing too
                    float xSign = width < 0 ? -1 : 1;
                    float ySign = height < 0 ? -1 : 1;

                    patch.draw(batch,
                            renderPosition.x - 0.5f * width * xSign, renderPosition.y - 0.5f * height * ySign,
                            0.5f * width * xSign, 0.5f * height * ySign,
                            Math.abs(width), Math.abs(height),
                            xSign * transformComponent.scale.x, ySign * transformComponent.scale.y,
                            transformComponent.rotation);
                } else if(spriteRenderer.renderMode == SpriteRendererComponent.RenderMode.tiled) {
                    textureRegion.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

                    float repeatX = width / (textureRegion.getTexture().getWidth() / metadata.pixelsPerUnit);
                    float repeatY = height / (textureRegion.getTexture().getHeight() / metadata.pixelsPerUnit);
                    textureRegion.setRegion(0, 0, repeatX, repeatY);

                    batch.draw(textureRegion,
                            renderPosition.x - 0.5f, renderPosition.y - 0.5f,
                            0.5f, 0.5f,
                            1f, 1f,
                            width, height,
                            transformComponent.rotation);
                } else if(spriteRenderer.renderMode == SpriteRendererComponent.RenderMode.simple) {
                    textureRegion.getTexture().setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
                    textureRegion.setRegion(0, 0, textureRegion.getTexture().getWidth(), textureRegion.getTexture().getHeight());

                    batch.draw(textureRegion,
                            renderPosition.x - 0.5f, renderPosition.y - 0.5f,
                            0.5f, 0.5f,
                            1f, 1f,
                            width, height,
                            transformComponent.rotation);
                }

                batch.setColor(Color.WHITE);
            }
        }

    }

    private NinePatch obtainNinePatch (Texture texture, SpriteMetadata metadata) {
        if(false && patchCache.containsKey(texture)) { //something better, maybe hash on pixel size + texture for this
            return patchCache.get(texture);
        } else {
            NinePatch patch = new NinePatch(texture, metadata.borderData[0], metadata.borderData[1], metadata.borderData[2], metadata.borderData[3]);
            patch.scale(1/metadata.pixelsPerUnit, 1/metadata.pixelsPerUnit); // fix this later
            patchCache.put(texture, patch);
            return patch;
        }
    }

    private ParticleEffectInstance obtainParticle (GameObject gameObject, ParticleEffectDescriptor descriptor) {
        ParticleComponent component = gameObject.getComponent(ParticleComponent.class);

        if(particleCache.containsKey(component)) {
            return particleCache.get(component);
        } else {
            ParticleEffectInstance instance = descriptor.createEffectInstance();
            particleCache.put(component, instance);
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

        TransformComponent transform = gameObject.getComponent(TransformComponent.class);
        float xSign = transform.scale.x < 0 ? -1: 1;
        float ySign = transform.scale.y < 0 ? -1: 1;

        vec.set(points[RT]).sub(points[LB]).scl(0.5f).add(points[LB]); // midpoint
        transformComponent.position.set(vec);
        vec.set(points[RT]).sub(points[LB]);
        transformComponent.scale.set(points[RT].dst(points[LT]) * xSign, points[RT].dst(points[RB]) * ySign);
        vec.set(points[RT]).sub(points[LT]).angleDeg();
        transformComponent.rotation = vec.angleDeg();

        if(xSign < 0) transformComponent.rotation -= 180;
        if(ySign < 0) transformComponent.rotation += 0;


        return transformComponent;
    }

    private Vector2 getWorldLocAround(GameObject gameObject, Vector2 point, float x, float y) {
        point.set(x, y);
        TransformComponent.localToWorld(gameObject, point);

        return point;
    }

    @EventHandler
    public void onComponentUpdated(ComponentUpdated event) {
        if(event.getComponent() instanceof ParticleComponent) {
            particleCache.remove((ParticleComponent)event.getComponent());
        }
    }
}
