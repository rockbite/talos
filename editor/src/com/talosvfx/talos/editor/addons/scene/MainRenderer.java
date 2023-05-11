package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.assets.GameResourceOwner;
import com.talosvfx.talos.editor.addons.scene.events.ComponentUpdated;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectContainer;
import com.talosvfx.talos.runtime.scene.GameObjectRenderer;
import com.talosvfx.talos.runtime.maps.GridPosition;
import com.talosvfx.talos.runtime.maps.StaticTile;
import com.talosvfx.talos.editor.addons.scene.utils.EntitySelectionBuffer;
import com.talosvfx.talos.editor.addons.scene.utils.PolyBatchWithEncodingOverride;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.runtime.scene.Scene;
import com.talosvfx.talos.runtime.scene.SceneLayer;
import com.talosvfx.talos.runtime.scene.components.ParticleComponent;
import com.talosvfx.talos.runtime.scene.components.RendererComponent;
import com.talosvfx.talos.runtime.scene.components.RoutineRendererComponent;
import com.talosvfx.talos.runtime.scene.components.SpriteRendererComponent;
import com.talosvfx.talos.runtime.scene.components.TileDataComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;
import com.talosvfx.talos.runtime.scene.render.RenderState;
import com.talosvfx.talos.runtime.scene.render.RenderStrategy;
import com.talosvfx.talos.runtime.scene.utils.TransformSettings;
import com.talosvfx.talos.runtime.vfx.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.vfx.ParticleEffectInstance;

import java.util.Comparator;

public class MainRenderer implements Observer {

    public float timeScale = 1f;

    private TransformComponent tempTransform = new TransformComponent();
    private Vector2 vec = new Vector2();
    private Vector2 vector2 = new Vector2();

    private Vector2[] points = new Vector2[4];

    private static final int LB = 0;
    private static final int LT = 1;
    private static final int RT = 2;
    private static final int RB = 3;

    private ObjectMap<ParticleComponent, ParticleEffectInstance> particleCache = new ObjectMap<>();

    private ShapeRenderer shapeRenderer;


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



    private GameObjectRenderer gameObjectRenderer;

    public MainRenderer() {
        for (int i = 0; i < 4; i++) {
            points[i] = new Vector2();
        }

        Notifications.registerObserver(this);

        shapeRenderer = new ShapeRenderer();

        gameObjectRenderer = new GameObjectRenderer();


        white = new Texture(Gdx.files.internal("white.png"));
    }





    public void update (GameObject root) {
        float delta = Gdx.graphics.getDeltaTime();
        gameObjectRenderer.update(root, delta);
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

    public void render (PolygonBatch batch, RenderState state, GameObjectContainer container) {
        render(batch, state, container.getSelfObject());
    }



    public void render (PolygonBatch batch, RenderState state, GameObject root) {
        temp.clear();
        temp.add(root);
        render(batch, state, temp);
    }
    public void render (PolygonBatch batch, RenderState state, Array<GameObject> rootObjects) {
        gameObjectRenderer.setCamera(this.camera);
        gameObjectRenderer.setSkipUpdates(skipUpdates);
        //fill entities
        gameObjectRenderer.buildRenderState(batch, state, rootObjects);


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

                    TransformSettings transformSettings = gameObject.getTransformSettings();


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

            if (gameObject.hasComponent(RoutineRendererComponent.class) && renderingToEntitySelectionBuffer) {
                continue;
            } else {
                gameObjectRenderer.renderObject(batch, gameObject);
            }
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
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



    public void renderStaticTileDynamic (StaticTile staticTile, Batch batch, float tileSizeX, float tileSizeY) {
        GridPosition gridPosition = staticTile.getGridPosition();
        GameAsset<?> staticTilesAsset = staticTile.getStaticTilesAsset();
        if (staticTilesAsset.type == GameAssetType.SPRITE) {
            GameAsset<AtlasSprite> texGameAsset = (GameAsset<AtlasSprite>)staticTilesAsset;
            Texture resource = texGameAsset.getResource().getTexture();

            batch.draw(resource, gridPosition.getIntX(), gridPosition.getIntY(), tileSizeX, tileSizeY);
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
//        talosRenderer.setCamera(camera);
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
