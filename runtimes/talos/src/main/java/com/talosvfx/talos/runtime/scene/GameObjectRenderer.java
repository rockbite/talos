package com.talosvfx.talos.runtime.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.CharArray;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.esotericsoftware.spine.Bone;
import com.talosvfx.talos.runtime.scene.components.*;
import com.talosvfx.talos.runtime.scene.render.*;
import com.talosvfx.talos.runtime.RuntimeContext;
import lombok.Getter;

import java.util.Comparator;

public class GameObjectRenderer implements Disposable {
    private ComponentRenderer<SpriteRendererComponent> spriteRenderer;
    private ComponentRenderer<MapComponent> mapRenderer;
    private ComponentRenderer<ParticleComponent<?>> particleRenderer;
    private ComponentRenderer<RoutineRendererComponent<?>> routineRenderer;
    private ComponentRenderer<SpineRendererComponent> spineRenderer;
    private ComponentRenderer<PathRendererComponent> pathRenderer;

    public final Comparator<GameObject> layerAndDrawOrderComparator;
    public final Comparator<GameObject> yDownDrawOrderComparator;

    public final Comparator<GameObject> parentSorter;

    private Camera camera;

    @Getter
    private boolean skipUpdates;


    private static TextureRegion brokenRegion;
    private static Texture brokenTexture;

    public GameObjectRenderer () {
        if (brokenRegion == null) {
            brokenTexture = new Texture(Gdx.files.classpath("missing.png"));
            brokenRegion = new TextureRegion(brokenTexture);
        }

        spriteRenderer = createSpriteRenderer();
        mapRenderer = createMapRenderer();
        particleRenderer = createParticleRenderer();
        routineRenderer = createRoutineRenderer();
        spineRenderer = createSpineRenderer();
        pathRenderer = createPathRenderer();

        layerAndDrawOrderComparator = new Comparator<GameObject>() {
            @Override
            public int compare (GameObject o1, GameObject o2) {
                float aSort = GameObjectRenderer.getDrawOrderSafe(o1);
                float bSort = GameObjectRenderer.getDrawOrderSafe(o2);
                return Float.compare(aSort, bSort);
            }
        };

        yDownDrawOrderComparator = new Comparator<GameObject>() {
            @Override
            public int compare (GameObject o1, GameObject o2) {
                float aSort = GameObjectRenderer.getBottomY(o1);
                float bSort = GameObjectRenderer.getBottomY(o2);
                return -Float.compare(aSort, bSort);
            }
        };

        parentSorter = new Comparator<GameObject>() {
            @Override
            public int compare (GameObject o1, GameObject o2) {
                SceneLayer o1Layer = GameObjectRenderer.getLayerSafe(o1);
                SceneLayer o2Layer = GameObjectRenderer.getLayerSafe(o2);

                if (o1Layer.equals(o2Layer)) {

                    RenderStrategy renderStrategy = o1Layer.getRenderStrategy();


                    Comparator<GameObject> sorter = getSorter(renderStrategy);

                    return sorter.compare(o1, o2);
                } else {
                    return Integer.compare(o1Layer.getIndex(), o2Layer.getIndex());
                }
            }
        };

    }

    private Comparator<GameObject> getSorter (RenderStrategy renderMode) {
        switch (renderMode) {
            case SCENE:
                return layerAndDrawOrderComparator;
            case YDOWN:
                return yDownDrawOrderComparator;
        }

        throw new GdxRuntimeException("No sorter found for render mode: " + renderMode);
    }

    private static float getBottomY (GameObject gameObject) {
        if (gameObject.hasComponentType(RendererComponent.class)) {
            RendererComponent componentAssignableFrom = gameObject.getComponentAssignableFrom(RendererComponent.class);
            TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);

            float y = transformComponent.worldPosition.y;


            if (componentAssignableFrom instanceof SpriteRendererComponent) {
                Vector2 size = ((SpriteRendererComponent) componentAssignableFrom).size;
                Vector2 worldScale = transformComponent.worldScale;

                float totalHeight = size.y * worldScale.y;
                y -= totalHeight / 2f;
            }

            if (componentAssignableFrom instanceof RendererComponent) {
                float fakeOffsetY = componentAssignableFrom.fakeOffsetY;
                y += fakeOffsetY;
            }

            return y;

        } else {
            if (gameObject.hasTransformComponent()) {
                TransformComponent component = gameObject.getTransformComponent();
                return component.worldPosition.y;
            }
        }

        return 0;
    }

    private static SceneLayer getLayerSafe (GameObject gameObject) {
        if (gameObject.hasComponentType(RendererComponent.class)) {
            RendererComponent rendererComponent = gameObject.getComponentAssignableFrom(RendererComponent.class);
            return rendererComponent.sortingLayer;
        }

        return RuntimeContext.getInstance().sceneData.getPreferredSceneLayer();
    }

    public static float getDrawOrderSafe (GameObject gameObject) {
        if (gameObject.hasComponentType(RendererComponent.class)) {
            RendererComponent rendererComponent = gameObject.getComponentAssignableFrom(RendererComponent.class);
            return rendererComponent.orderingInLayer;
        }

        return -55;
    }


    protected ComponentRenderer<MapComponent> createMapRenderer () {
        return new MapComponentRenderer(this);
    }

    protected ComponentRenderer<RoutineRendererComponent<?>> createRoutineRenderer () {
        return new RoutineComponentRenderer(this);
    }

    protected ComponentRenderer<SpineRendererComponent> createSpineRenderer () {
        return new SkeletonComponentRenderer(this);
    }

    protected ComponentRenderer<ParticleComponent<?>> createParticleRenderer () {
        return new SimpleParticleComponentRenderer(this);
    }

    protected ComponentRenderer<SpriteRendererComponent> createSpriteRenderer () {
        return new SpriteComponentRenderer(this);
    }

    protected ComponentRenderer<PathRendererComponent> createPathRenderer () {
        return new PathComponentRenderer(this);
    }

    protected void sort (Array<GameObject> list) {
        list.sort(parentSorter);
    }

    public static void debugTransforms (GameObject gameObject, int indent) {
        if (gameObject.hasComponent(TransformComponent.class)) {
            TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
            CharArray builder = new CharArray();

            //Indent all by indent  * 2 spaces
            //print in format
            //name
            //    local: [Position]: (x,y,z), [Scale]: (x,y,z), [Rotation]: (x,y,z)
            //    world: [Position]: (x,y,z), [Scale]: (x,y,z), [Rotation]: (x,y,z)

            for (int i = 0; i < indent; i++) {
                builder.append("  ");
            }
            builder.append(gameObject.getName());
            builder.append("\n");
            for (int i = 0; i < indent; i++) {
                builder.append("  ");
            }
            builder.append("  local: [Position]: ");
            builder.append(transformComponent.position);
            builder.append(", [Scale]: ");
            builder.append(transformComponent.scale);
            builder.append(", [Rotation]: ");
            builder.append(transformComponent.rotation);
            builder.append("\n");

            for (int i = 0; i < indent; i++) {
                builder.append("  ");
            }
            builder.append("  world: [Position]: ");
            builder.append(transformComponent.worldPosition);
            builder.append(", [Scale]: ");
            builder.append(transformComponent.worldScale);
            builder.append(", [Rotation]: ");
            builder.append(transformComponent.worldRotation);
            builder.append("\n");

            if (gameObject.hasComponent(ParticleComponent.class)) {
                ParticleComponent particleComponent = gameObject.getComponent(ParticleComponent.class);
                if (particleComponent.getEffectRef() != null) {
                    float worldRotation = particleComponent.getEffectRef().getWorldRotation();
                    Vector2 worldScale = particleComponent.getEffectRef().getWorldScale();
                    //print it
                    for (int i = 0; i < indent; i++) {
                        builder.append("  ");
                    }
                    builder.append("  particle: [Position]: ");
                    builder.append(particleComponent.getEffectRef().getPosition());
                    builder.append(", [Scale]: ");
                    builder.append(worldScale);
                    builder.append(", [Rotation]: ");
                    builder.append(worldRotation);


                }

            }
             System.out.println(builder);

        }
        for (GameObject object : gameObject.getGameObjects()) {
            debugTransforms(object, indent + 1);
        }
    }

    public void update (GameObject gameObject, float delta) {
        if (!gameObject.active || !gameObject.isEditorVisible())
            return;

        //If we are a skeleton, we update it now
        if (gameObject.hasSpineComponent()) {
            SpineRendererComponent spineRendererComponent = gameObject.getSpineComponent();
            spineRenderer.update(gameObject, spineRendererComponent, delta);

            // update bone game objects
            if (spineRendererComponent.generateGameObjectBones) {
                Array<GameObject> boneGOs = gameObject.getChildrenWithBoneComponent();

                for (GameObject boneGO : boneGOs) {
                    BoneComponent boneComponent = boneGO.getBoneComponent();
                    Bone bone = boneComponent.getBone();
                    TransformComponent transform = boneGO.getTransformComponent();

                    transform.worldScale.set(bone.getWorldScaleX(), bone.getWorldScaleY());
                    transform.worldRotation = bone.localToWorldRotation(bone.getRotation());
                    transform.worldPosition.set(bone.getWorldX(), bone.getWorldY());

                    transform.position.set(bone.getX(), bone.getY());
                    transform.rotation = bone.getRotation();
                    transform.scale.set(bone.getScaleX(), bone.getScaleY());
                }
            }
        }
        if (gameObject.hasParticleComponent()) {
            ParticleComponent<?> particleComponent = gameObject.getParticleComponent();
            particleRenderer.update(gameObject, particleComponent, delta);
        }

        if (gameObject.hasTransformComponent() && !gameObject.hasBoneComponent()) {
            TransformComponent transform = gameObject.getTransformComponent();

            transform.worldPosition.set(transform.position);
            transform.worldScale.set(transform.scale);
            transform.worldRotation = transform.rotation;

            if (gameObject.parent != null) {

                if (gameObject.parent.hasTransformComponent()) {
                    //Combine our world with the parent

                    TransformComponent parentTransform = gameObject.parent.getTransformComponent();
                    transform.worldPosition.scl(parentTransform.worldScale);
                    transform.worldPosition.rotateDeg(parentTransform.worldRotation);
                    transform.worldPosition.add(parentTransform.worldPosition);

                    transform.worldRotation += parentTransform.worldRotation;
                    transform.worldScale.scl(parentTransform.worldScale);
                }
            }
        }

        // if root has render component try mixing colors if they exist
        if (gameObject.hasComponentType(RendererComponent.class)) {
            final RendererComponent rendererComponent = gameObject.getComponentAssignableFrom(RendererComponent.class);

            // check if render component has color value
            if (rendererComponent instanceof IColorHolder) {
                final IColorHolder colorHolder = (IColorHolder) rendererComponent;

                // update final color by Renderer color
                final Color finalColor = (colorHolder.getFinalColor());
                finalColor.set(colorHolder.getColor());

                // should inherit parent color update final color by parent color
                if (colorHolder.shouldInheritParentColor()) {
                    if (gameObject.parent != null) {
                        // check if parent contains render component
                        if (gameObject.parent.hasComponentType(RendererComponent.class)) {
                            final RendererComponent parentRendererComponent = gameObject.parent.getComponentAssignableFrom(RendererComponent.class);

                            // check if parent render component has color value
                            if (parentRendererComponent instanceof IColorHolder) {
                                // combine colors
                                finalColor.mul(((IColorHolder) parentRendererComponent).getFinalColor());
                            }
                        }
                    }
                }
            }
        }

        if (gameObject.getGameObjects() != null) {
            for (int i = 0; i < gameObject.getGameObjects().size; i++) {
                GameObject child = gameObject.getGameObjects().get(i);
                update(child, delta);
            }
        }
    }

    protected void fillRenderableEntities (Array<GameObject> rootObjects, Array<GameObject> list) {
        for (GameObject root : rootObjects) {
            if (!root.active || !root.isEditorVisible()) continue;

            boolean childrenVisibleFlag = true;
            if (root.hasComponentType(RendererComponent.class)) {
                RendererComponent rendererComponent = root.getComponentAssignableFrom(RendererComponent.class);
                childrenVisibleFlag = rendererComponent.childrenVisible;
                if (rendererComponent.visible) {
                    list.add(root);
                }
            }
            if (childrenVisibleFlag) {
                if (root.getGameObjects() != null) {
                    fillRenderableEntities(root.getGameObjects(), list);
                }
            }
        }

    }


    public static void renderBrokenComponent (Batch batch, GameObject gameObject, TransformComponent transformComponent) {

        float width = 1f;
        float height = 1f;
        if (gameObject.hasComponent(SpriteRendererComponent.class)) {
            SpriteRendererComponent component = gameObject.getComponent(SpriteRendererComponent.class);
            width = component.size.x;
            height = component.size.y;
        }

        batch.draw(brokenRegion,
                transformComponent.worldPosition.x - 0.5f, transformComponent.worldPosition.y - 0.5f,
                0.5f, 0.5f,
                1f, 1f,
                width * transformComponent.worldScale.x, height * transformComponent.worldScale.y,
                transformComponent.worldRotation);
    }


    Array<GameObject> temp = new Array<>();


    public void buildRenderState (PolygonBatch batch, RenderState state, Array<GameObject> rootObjects) {
        boolean hiearchyDirty = false;
        for (GameObject rootObject : rootObjects) {
            if (rootObject.hierarchyDirty) {
                hiearchyDirty = true;

                //reset it because we gonna sort it this pass
                rootObject.hierarchyDirty = false;
            }
        }
        if (hiearchyDirty) {
            state.list.clear();
            fillRenderableEntities(rootObjects, state.list);
        }
        sort(state.list);
    }


    public void renderObject (Batch batch, GameObject gameObject) {
        if (gameObject.hasRoutineRendererComponent()) {
            routineRenderer.render(batch, camera, gameObject, gameObject.getRoutineRendererComponent());
        }

        if (gameObject.hasSpriteComponent()) {
            spriteRenderer.render(batch, camera, gameObject, gameObject.getSpriteComponent());
        } else if (gameObject.hasParticleComponent()) {
            particleRenderer.render(batch, camera, gameObject, gameObject.getParticleComponent());
        } else if (gameObject.hasSpineComponent()) {
            spineRenderer.render(batch, camera, gameObject, gameObject.getSpineComponent());
        } else if (gameObject.hasComponent(MapComponent.class)) {
            mapRenderer.render(batch, camera, gameObject, gameObject.getComponent(MapComponent.class));
        } else if (gameObject.hasComponent(PathRendererComponent.class)) {
            pathRenderer.render(batch, camera, gameObject, gameObject.getComponent(PathRendererComponent.class));
        }

    }

    public void buildRenderStateAndRender (PolygonBatch batch, Camera camera, RenderState state, GameObject root) {
        temp.clear();
        temp.add(root);
        buildRenderStateAndRender(batch, camera, state, temp);
    }

    public void buildRenderStateAndRender (PolygonBatch batch, Camera camera, RenderState state, Array<GameObject> rootObjects) {
        setCamera(camera);

        buildRenderState(batch, state, rootObjects);
        for (int i = 0; i < state.list.size; i++) {
            GameObject gameObject = state.list.get(i);
            renderObject(batch, gameObject);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void setCamera (Camera camera) {
        this.camera = camera;
    }

    /**
     * Any renderers that may want to skip updates do it here
     *
     * @param skipUpdates
     */
    public void setSkipUpdates (boolean skipUpdates) {
        this.skipUpdates = skipUpdates;
    }

    @Override
    public void dispose () {
		if (brokenRegion != null) {
            brokenTexture.dispose();
			brokenRegion = null;
            brokenTexture = null;
		}
    }
}
