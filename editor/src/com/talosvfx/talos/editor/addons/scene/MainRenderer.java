package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.spine.TalosSkeletonRenderer;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineRenderer;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.assets.RawAsset;
import com.talosvfx.talos.editor.addons.scene.events.ComponentUpdated;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.*;
import com.talosvfx.talos.editor.addons.scene.maps.GridPosition;
import com.talosvfx.talos.editor.addons.scene.maps.StaticTile;
import com.talosvfx.talos.editor.addons.scene.maps.TalosMapRenderer;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.EntitySelectionBuffer;
import com.talosvfx.talos.editor.addons.scene.utils.PolyBatchWithEncodingOverride;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpriteMetadata;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.Gizmo;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.serialization.VFXProjectData;
import com.talosvfx.talos.runtime.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.ParticleEffectInstance;
import com.talosvfx.talos.runtime.render.SpriteBatchParticleRenderer;

import java.util.Comparator;

public class MainRenderer implements Observer {

    public static SceneLayer DEFAULT_SCENE_LAYER = new SceneLayer("Default", 0);

    public final Comparator<GameObject> layerAndDrawOrderComparator;

    public float timeScale = 1f;

    private  Comparator<GameObject> activeSorter;

    private TransformComponent tempTransform = new TransformComponent();
    private Vector2 vec = new Vector2();
    private Vector2 vector2 = new Vector2();

    private Vector2[] points = new Vector2[4];

    private static final int LB = 0;
    private static final int LT = 1;
    private static final int RT = 2;
    private static final int RB = 3;

    private ObjectMap<Texture, NinePatch> patchCache = new ObjectMap<>();
    private ObjectMap<ParticleComponent, ParticleEffectInstance> particleCache = new ObjectMap<>();

    private SpriteBatchParticleRenderer talosRenderer;
    private TalosSkeletonRenderer spineRenderer;

    private TalosMapRenderer mapRenderer;
    private ShapeRenderer shapeRenderer;

    private RoutineRenderer routineRenderer;

    private TextureRegion textureRegion = new TextureRegion();
    private Camera camera;

    private boolean renderParentTiles = false;
    private boolean renderingToEntitySelectionBuffer = false;

    public boolean skipUpdates = false;

    private Texture white;
    private Array<SceneLayer> layerList;

    public void setLayers (Array<SceneLayer> layerList) {
        this.layerList = layerList;
    }

    public static class RenderState {
        private Array<GameObject> list = new Array<>();
    }

    public MainRenderer() {
        for (int i = 0; i < 4; i++) {
            points[i] = new Vector2();
        }

        Notifications.registerObserver(this);

        talosRenderer = new SpriteBatchParticleRenderer(camera);
        spineRenderer = new TalosSkeletonRenderer();
        mapRenderer = new TalosMapRenderer();
        shapeRenderer = new ShapeRenderer();
        routineRenderer = new RoutineRenderer();

        layerAndDrawOrderComparator = new Comparator<GameObject>() {
            @Override
            public int compare (GameObject o1, GameObject o2) {
                SceneLayer o1Layer = MainRenderer.getLayerSafe(o1);
                SceneLayer o2Layer = MainRenderer.getLayerSafe(o1);

                if (o1Layer.equals(o2Layer)) {
                    float aSort = MainRenderer.getDrawOrderSafe(o1);
                    float bSort = MainRenderer.getDrawOrderSafe(o2);
                    return Float.compare(aSort, bSort);
                } else {
                    return Integer.compare(o1Layer.getIndex(), o2Layer.getIndex());
                }
            }
        };

        activeSorter = layerAndDrawOrderComparator;
        white = new Texture(Gdx.files.internal("white.png"));
    }

    private static SceneLayer getLayerSafe(GameObject gameObject) {
        if (gameObject.hasComponentType(RendererComponent.class)) {
            RendererComponent rendererComponent = gameObject.getComponentAssignableFrom(RendererComponent.class);
            return rendererComponent.sortingLayer;
        }
        return DEFAULT_SCENE_LAYER;
    }

    public static float getDrawOrderSafe (GameObject gameObject) {
        if (gameObject.hasComponentType(RendererComponent.class)) {
            RendererComponent rendererComponent = gameObject.getComponentAssignableFrom(RendererComponent.class);
            return rendererComponent.orderingInLayer;
        }
        return -55;
    }

    public void setActiveSorter (Comparator<GameObject> customSorter) {
        this.activeSorter = customSorter;
    }

    public void update (GameObject root) {
        if (!root.active || !root.isEditorVisible()) return;
        if (root.hasComponent(TransformComponent.class)) {
            TransformComponent transform = root.getComponent(TransformComponent.class);

            transform.worldPosition.set(transform.position);
            transform.worldScale.set(transform.scale);
            transform.worldRotation = transform.rotation;

            if (root.parent != null) {

                if (root.parent.hasComponent(TransformComponent.class)) {
                    //Combine our world with the parent

                    TransformComponent parentTransform = root.parent.getComponent(TransformComponent.class);
                    transform.worldPosition.scl(parentTransform.worldScale);
                    transform.worldPosition.rotateDeg(parentTransform.worldRotation);
                    transform.worldPosition.add(parentTransform.worldPosition);

                    transform.worldRotation += parentTransform.worldRotation;
                    transform.worldScale.scl(parentTransform.worldScale);
                }
            }
        }

        if (root.getGameObjects() != null) {
            for (int i = 0; i < root.getGameObjects().size; i++) {
                GameObject child = root.getGameObjects().get(i);
                update(child);
            }
        }
    }

    private void fillRenderableEntities (Array<GameObject> rootObjects, Array<GameObject> list) {
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


    Array<GameObject> temp = new Array<>();
    public void render (PolygonBatch batch, RenderState state, GameObject root) {
        temp.clear();
        temp.add(root);
        render(batch, state, temp);
    }
    public void render (PolygonBatch batch, RenderState state, Array<GameObject> rootObjects) {
        mapRenderer.setCamera(this.camera);

        //fill entities
        state.list.clear();
        fillRenderableEntities(rootObjects, state.list);
        sort(state.list);

        batch.end();
        if (renderParentTiles) {

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Color color = Color.valueOf("459534");
            color.a = 0.5f;
            shapeRenderer.setColor(color);
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);


            for (int i = 0; i < state.list.size; i++) {
                GameObject gameObject = state.list.get(i);
                if (gameObject.hasComponent(TileDataComponent.class)) {

                    TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);

                    Gizmo.TransformSettings transformSettings = gameObject.getTransformSettings();

                    TileDataComponent tileDataComponent = gameObject.getComponent(TileDataComponent.class);
                    GridPosition bottomLeftParentTile = tileDataComponent.getBottomLeftParentTile();

                    for (GridPosition parentTile : tileDataComponent.getParentTiles()) {

                        if (gameObject.isPlacing) {
                            float posY = parentTile.getIntY();
                            float posX = parentTile.getIntX();

                            posX -= transformSettings.transformOffsetX;
                            posY -= transformSettings.transformOffsetY;

                            posX += transformComponent.worldPosition.x;
                            posY += transformComponent.worldPosition.y;

                            posX -= transformSettings.offsetX;
                            posY -= transformSettings.offsetY;

                            shapeRenderer.rect(posX, posY, 1,1);

                        } else {
                            float posY = parentTile.getIntY();
                            float posX = parentTile.getIntX();

                            posX -= tileDataComponent.getVisualOffset().x;
                            posY -= tileDataComponent.getVisualOffset().y;

                            posX += transformComponent.worldPosition.x;
                            posY += transformComponent.worldPosition.y;

                            posX -= bottomLeftParentTile.getIntX();
                            posY -= bottomLeftParentTile.getIntY();

                            shapeRenderer.rect(posX, posY, 1,1);

                        }



                    }
                }

            }
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
        batch.begin();

        if (renderingToEntitySelectionBuffer) {
            //Render with batch

            for (int i = 0; i < state.list.size; i++) {
                GameObject gameObject = state.list.get(i);
                if (gameObject.hasComponent(TileDataComponent.class)) {

                    if (batch instanceof PolyBatchWithEncodingOverride) {
                        Color colourForEntityUUID = EntitySelectionBuffer.getColourForEntityUUID(gameObject);
                        ((PolyBatchWithEncodingOverride)batch).setCustomEncodingColour(colourForEntityUUID.r, colourForEntityUUID.g, colourForEntityUUID.b, colourForEntityUUID.a);
                    }

                    TileDataComponent tileDataComponent = gameObject.getComponent(TileDataComponent.class);

                    for (GridPosition parentTile : tileDataComponent.getParentTiles()) {
                        batch.draw(white, parentTile.x, parentTile.y, 1, 1);

                    }
                }

            }
        }

        for (int i = 0; i < state.list.size; i++) {
            GameObject gameObject = state.list.get(i);


            if (batch instanceof PolyBatchWithEncodingOverride) {
                Color colourForEntityUUID = EntitySelectionBuffer.getColourForEntityUUID(gameObject);
                ((PolyBatchWithEncodingOverride)batch).setCustomEncodingColour(colourForEntityUUID.r, colourForEntityUUID.g, colourForEntityUUID.b, colourForEntityUUID.a);
            }


            GameResourceOwner<?> resourceComponent = gameObject.getRenderResourceComponent();
            if (resourceComponent != null) {
                //Its something with a game resource

                GameAsset<?> gameResource = resourceComponent.getGameResource();
                if (gameResource == null || gameResource.isBroken()) {
                    //Render the broken sprite

                    renderBrokenComponent(batch, gameObject, gameObject.getComponent(TransformComponent.class));
                    continue;
                }
            }

            if(gameObject.hasComponent(SpriteRendererComponent.class)) {
                renderSprite(batch, gameObject);
            } else if(gameObject.hasComponent(ParticleComponent.class)) {
                renderParticle(batch, gameObject);
            } else if(gameObject.hasComponent(SpineRendererComponent.class)) {
                renderSpine(batch, gameObject);
            } else if(gameObject.hasComponent(MapComponent.class)) {
                renderMap(batch, gameObject);
            } else if(gameObject.hasComponent(RoutineRendererComponent.class)) {
                if (!renderingToEntitySelectionBuffer) {
                    renderWithRoutine(batch, gameObject);
                }
            }
        }
    }



    private void renderBrokenComponent (Batch batch, GameObject gameObject, TransformComponent transformComponent) {

        float width = 1f;
        float height = 1f;
        if (gameObject.hasComponent(SpriteRendererComponent.class)) {
            SpriteRendererComponent component = gameObject.getComponent(SpriteRendererComponent.class);
            width = component.size.x;
            height = component.size.y;
        }

        batch.draw(AssetRepository.getInstance().brokenTextureRegion,
                transformComponent.worldPosition.x - 0.5f, transformComponent.worldPosition.y - 0.5f,
                0.5f, 0.5f,
                1f, 1f,
                width * transformComponent.worldScale.x, height * transformComponent.worldScale.y,
                transformComponent.worldRotation);
    }

    private void renderSpine (Batch batch, GameObject gameObject) {
        TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
        SpineRendererComponent spineRendererComponent = gameObject.getComponent(SpineRendererComponent.class);

        spineRendererComponent.skeleton.setPosition(transformComponent.worldPosition.x, transformComponent.worldPosition.y);
        spineRendererComponent.skeleton.setScale(transformComponent.worldScale.x * spineRendererComponent.scale, transformComponent.worldScale.y * spineRendererComponent.scale);

        if (!skipUpdates) {
            spineRendererComponent.animationState.update(Gdx.graphics.getDeltaTime() * timeScale);
            spineRendererComponent.animationState.apply(spineRendererComponent.skeleton);
        }
        spineRendererComponent.skeleton.updateWorldTransform();

        spineRendererComponent.skeleton.getColor().set(spineRendererComponent.color);
        spineRenderer.draw(batch, spineRendererComponent.skeleton);

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void renderParticle (PolygonBatch batch, GameObject gameObject) {
        TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
        ParticleComponent particleComponent = gameObject.getComponent(ParticleComponent.class);

        VFXProjectData resource = particleComponent.gameAsset.getResource();
        ParticleEffectDescriptor descriptor = resource.getDescriptorSupplier().get();

        if (descriptor == null) return;

        ParticleEffectInstance instance = obtainParticle(gameObject, descriptor);
        instance.setPosition(transformComponent.worldPosition.x, transformComponent.worldPosition.y, 0);

        if (!skipUpdates) {
            instance.update(Gdx.graphics.getDeltaTime() * timeScale);
        }
        talosRenderer.setBatch(batch);
        talosRenderer.render(instance);
    }

    private void renderSprite (Batch batch, GameObject gameObject) {
        TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);

        SpriteRendererComponent spriteRenderer = gameObject.getComponent(SpriteRendererComponent.class);
        GameAsset<Texture> gameResource = spriteRenderer.getGameResource();
        RawAsset rootRawAsset = gameResource.getRootRawAsset();
        AMetadata metaData = rootRawAsset.metaData;
        if (metaData instanceof SpriteMetadata) {
            //It should be
            SpriteMetadata metadata = (SpriteMetadata)metaData;

            Texture resource = spriteRenderer.getGameResource().getResource();
            if (resource.getMagFilter() != metadata.magFilter || resource.getMinFilter() != metadata.minFilter) {
                resource.setFilter(metadata.minFilter, metadata.magFilter);
            }
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

                    float pivotX = transformComponent.pivot.x;
                    float pivotY = transformComponent.pivot.y;

                    patch.draw(batch,
                            transformComponent.worldPosition.x - pivotX * width * xSign, transformComponent.worldPosition.y - pivotY * height * ySign,
                            pivotX * width * xSign, pivotY * height * ySign,
                            Math.abs(width), Math.abs(height),
                            xSign * transformComponent.worldScale.x, ySign * transformComponent.worldScale.y,
                            transformComponent.worldRotation);
                } else if(spriteRenderer.renderMode == SpriteRendererComponent.RenderMode.tiled) {


                    //Tiled mode, we draw from bottom left and fill it based on tile size

                    float tileWidth = spriteRenderer.tileSize.x * transformComponent.worldScale.x;
                    float tileHeight = spriteRenderer.tileSize.y * transformComponent.worldScale.y;

                    float totalWidth = width * transformComponent.worldScale.x;
                    float totalHeight = height * transformComponent.worldScale.y;

                    float startX = transformComponent.worldPosition.x - totalWidth/2;
                    float startY = transformComponent.worldPosition.y - totalHeight/2;

                    float xCoord = 0;
                    float yCoord = 0;

                    float halfTileWidth = tileWidth / 2f;
                    float halfTileHeight = tileHeight / 2f;

                    float endX = startX + totalWidth;
                    float endY = startY + totalHeight;

                    xCoord = startX;
                    yCoord = startX;

                    for (xCoord = startX; xCoord < endX - tileWidth; xCoord += tileWidth){
                        for (yCoord = startY; yCoord < endY - tileHeight; yCoord += tileHeight) {

                            //Coord needs to be rotated from cneter

                            vector2.set(xCoord + halfTileWidth, yCoord + halfTileHeight);
                            vector2.sub(transformComponent.worldPosition);
                            vector2.rotateDeg(transformComponent.worldRotation);
                            vector2.add(transformComponent.worldPosition);

                            //Tiny scale for artifacting, better to do with a mesh really
                            batch.draw(textureRegion, vector2.x - halfTileWidth, vector2.y - halfTileHeight, halfTileWidth, halfTileHeight, tileWidth, tileHeight, 1.0002f, 1.002f, transformComponent.worldRotation);
                        }
                    }

                    float remainderX = endX - xCoord;
                    float remainderY = endY - yCoord;


                    //Draw the remainders in x
                    for (float yCoordRemainder = startY; yCoordRemainder < endY - tileHeight; yCoordRemainder += tileHeight) {

                        //Coord needs to be rotated from cneter

                        vector2.set(xCoord + halfTileWidth, yCoordRemainder + halfTileHeight);
                        vector2.sub(transformComponent.worldPosition);
                        vector2.rotateDeg(transformComponent.worldRotation);
                        vector2.add(transformComponent.worldPosition);

                        //clip it

                        float uWidth = textureRegion.getU2() - textureRegion.getU();
                        float uScale = uWidth * remainderX/tileWidth;
                        float cachedU2 = textureRegion.getU2();
                        textureRegion.setU2(textureRegion.getU() + uScale);
                        batch.draw(textureRegion, vector2.x - halfTileWidth, vector2.y - halfTileHeight, halfTileWidth, halfTileHeight, remainderX, tileHeight, 1.002f, 1.002f, transformComponent.worldRotation);
                        textureRegion.setU2(cachedU2);
                    }

                    //Draw the remainders in y
                    for (float xCoordRemainder = startX; xCoordRemainder < endX - tileWidth; xCoordRemainder += tileWidth) {

                        //Coord needs to be rotated from cneter

                        vector2.set(xCoordRemainder + halfTileWidth, yCoord + halfTileHeight);
                        vector2.sub(transformComponent.worldPosition);
                        vector2.rotateDeg(transformComponent.worldRotation);
                        vector2.add(transformComponent.worldPosition);

                        //clip it

                        float vWidth = textureRegion.getV2() - textureRegion.getV();
                        float vScale = vWidth * remainderY/tileHeight;
                        float cachedV = textureRegion.getV();
                        textureRegion.setV(textureRegion.getV2() - vScale);
                        batch.draw(textureRegion, vector2.x - halfTileWidth, vector2.y - halfTileHeight, halfTileWidth, halfTileHeight, tileWidth, remainderY, 1.002f, 1.002f, transformComponent.worldRotation);
                        textureRegion.setV(cachedV);
                    }

                    //Last one

                    {
                        vector2.set(xCoord + halfTileWidth, yCoord + halfTileHeight);
                        vector2.sub(transformComponent.worldPosition);
                        vector2.rotateDeg(transformComponent.worldRotation);
                        vector2.add(transformComponent.worldPosition);

                        //clip it

                        float uWidth = textureRegion.getU2() - textureRegion.getU();
                        float uScale = uWidth * remainderX/tileWidth;
                        float cachedU2 = textureRegion.getU2();
                        textureRegion.setU2(textureRegion.getU() + uScale);


                        float vWidth = textureRegion.getV2() - textureRegion.getV();
                        float vScale = vWidth * remainderY/tileHeight;
                        float cachedV = textureRegion.getV();
                        textureRegion.setV(textureRegion.getV2() - vScale);

                        batch.draw(textureRegion, vector2.x - halfTileWidth, vector2.y - halfTileHeight, halfTileWidth, halfTileHeight, remainderX, remainderY, 1.002f, 1.002f, transformComponent.worldRotation);
                        textureRegion.setV(cachedV);
                        textureRegion.setU2(cachedU2);

                    }

//
//                    batch.draw(textureRegion,
//                        transformComponent.worldPosition.x - 0.5f, transformComponent.worldPosition.y - 0.5f,
//                            0.5f, 0.5f,
//                            1f, 1f,
//                            width * transformComponent.worldScale.x, height * transformComponent.worldScale.y,
//                            transformComponent.worldRotation);
                } else if(spriteRenderer.renderMode == SpriteRendererComponent.RenderMode.simple) {

                    float pivotX = transformComponent.pivot.x;
                    float pivotY = transformComponent.pivot.y;

                    batch.draw(textureRegion,
                        transformComponent.worldPosition.x - pivotX, transformComponent.worldPosition.y - pivotY,
                            pivotX, pivotY,
                            1f, 1f,
                            width * transformComponent.worldScale.x, height * transformComponent.worldScale.y,
                            transformComponent.worldRotation);
                }

                batch.setColor(Color.WHITE);
            }
        }

    }

    private void renderWithRoutine (Batch batch, GameObject gameObject) {
        //We render the map with its own main renderer, its own sorter
        RoutineRendererComponent routineRendererComponent = gameObject.getComponent(RoutineRendererComponent.class);
        routineRenderer.render(this, batch, gameObject, routineRendererComponent);
    }

    private void renderMap (PolygonBatch batch, GameObject gameObject) {
        //We render the map with its own main renderer, its own sorter
        MapComponent map = gameObject.getComponent(MapComponent.class);

        mapRenderer.render(this, batch, gameObject, map);
    }

    public void renderStaticTileDynamic (StaticTile staticTile, Batch batch, float tileSizeX, float tileSizeY) {
        GridPosition gridPosition = staticTile.getGridPosition();
        GameAsset<?> staticTilesAsset = staticTile.getStaticTilesAsset();
        if (staticTilesAsset.type == GameAssetType.SPRITE) {
            GameAsset<Texture> texGameAsset = (GameAsset<Texture>)staticTilesAsset;
            Texture resource = texGameAsset.getResource();

            batch.draw(resource, gridPosition.getIntX(), gridPosition.getIntY(), tileSizeX, tileSizeY);
        }
    }

    private NinePatch obtainNinePatch (Texture texture, SpriteMetadata metadata) {
        if(patchCache.containsKey(texture)) { //something better, maybe hash on pixel size + texture for this
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
            ParticleEffectInstance particleEffectInstance = particleCache.get(component);
            component.setEffectRef(particleEffectInstance);
            return particleEffectInstance;
        } else {
            ParticleEffectInstance instance = descriptor.createEffectInstance();
            component.setEffectRef(instance);
            particleCache.put(component, instance);
            return instance;
        }
    }

    private void sort (Array<GameObject> list) {
        list.sort(activeSorter);
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
        tempTransform.position.set(vec);
        vec.set(points[RT]).sub(points[LB]);
        tempTransform.scale.set(points[RT].dst(points[LT]) * xSign, points[RT].dst(points[RB]) * ySign);
        vec.set(points[RT]).sub(points[LT]).angleDeg();
        tempTransform.rotation = vec.angleDeg();

        if(xSign < 0) tempTransform.rotation -= 180;
        if(ySign < 0) tempTransform.rotation += 0;


        return tempTransform;
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

        if (event.getComponent() instanceof RoutineRendererComponent) {
            RoutineRendererComponent routineRendererComponent = (RoutineRendererComponent) event.getComponent();
            routineRendererComponent.routineInstance.setDirty();
        }

        GameObject gameObject = event.getComponent().getGameObject();
        if (event.getComponent() instanceof TransformComponent && gameObject.hasComponent(RoutineRendererComponent.class)) {
            RoutineRendererComponent component = gameObject.getComponent(RoutineRendererComponent.class);
            component.routineInstance.setDirty();
        }
    }

    public void setCamera (Camera camera) {

        this.camera = camera;
        talosRenderer.setCamera(camera);
    }


    public void setRenderParentTiles (boolean renderParentTiles) {
        this.renderParentTiles = renderParentTiles;
    }

    public void setRenderingEntitySelectionBuffer (boolean renderingToBuffer) {
        this.renderingToEntitySelectionBuffer = renderingToBuffer;
    }

    public Texture getWhiteTexture() {
        return white;
    }

    public Camera getCamera() {
        return camera;
    }
}
