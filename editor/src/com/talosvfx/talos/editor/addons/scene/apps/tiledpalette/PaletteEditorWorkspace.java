package com.talosvfx.talos.editor.addons.scene.apps.tiledpalette;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.*;
import com.kotcrab.vis.ui.FocusManager;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.MainRenderer;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectSelectionChanged;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.TilePaletteData;
import com.talosvfx.talos.editor.addons.scene.logic.components.TileDataComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.maps.GridPosition;
import com.talosvfx.talos.editor.addons.scene.maps.StaticTile;
import com.talosvfx.talos.editor.addons.scene.maps.TalosLayer;
import com.talosvfx.talos.editor.addons.scene.utils.PolygonSpriteBatchMultiTexture;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.utils.GridDrawer;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;

import java.util.Comparator;
import java.util.UUID;
import java.util.function.Supplier;

import static com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace.ctrlPressed;

public class PaletteEditorWorkspace extends ViewportWidget implements Notifications.Observer {
    private PaletteEditor paletteEditor;
    GameAsset<TilePaletteData> paletteData;

    private GridDrawer gridDrawer;
    private SceneEditorWorkspace.GridProperties gridProperties;

    private Image selectionRect;

    private MainRenderer mainRenderer;

    private Pool<PaletteEvent> paletteEventPool;

    private float tmpHeightOffset;

    private boolean translatingMode = true;
    private GameObject selectedGameObject = null;

    private InputListener currentGizmoListener;

    private Comparator<GameObject> orthoTopDownSorter = new Comparator<GameObject>() {
        @Override
        public int compare (GameObject a, GameObject b) {

            GameObject rootDummy = paletteData.getResource().rootDummy;


            //do z sorting on elements at the top level
            if ((a.parent == null && b.parent == null) || (a.parent == rootDummy && b.parent == rootDummy)) {
                TransformComponent ATransform = a.getComponent(TransformComponent.class);
                TransformComponent BTransform = b.getComponent(TransformComponent.class);

                float AworldPosY = ATransform.worldPosition.y;
                float BworldPosY = BTransform.worldPosition.y;

                if (a.hasComponent(TileDataComponent.class)) {
                    AworldPosY += (a.getComponent(TileDataComponent.class).getFakeZ());
                }
                if (b.hasComponent(TileDataComponent.class)) {
                    BworldPosY += (b.getComponent(TileDataComponent.class).getFakeZ());
                }

                return -Float.compare(AworldPosY, BworldPosY);
            } else {
                float aSort = MainRenderer.getDrawOrderSafe(a);
                float bSort = MainRenderer.getDrawOrderSafe(b);

                return Float.compare(aSort, bSort);
            }

        }
    };
    private Comparator<GameAsset<?>> gameObjectRenderOrderComparator = new Comparator<GameAsset<?>>() {
        @Override
        public int compare (GameAsset<?> o1, GameAsset<?> o2) {
            OrderedMap<GameAsset<?>, GameObject> gameObjects = paletteData.getResource().gameObjects;

            GameObject a = gameObjects.get(o1);
            GameObject b = gameObjects.get(o2);

            return orthoTopDownSorter.compare(a, b);
        }
    };

    private static final Color parentTilesColor = Color.valueOf("#4A6DE5");

    public PaletteEditorWorkspace(PaletteEditor paletteEditor) {
        super();
        this.paletteEditor = paletteEditor;
        this.paletteData = paletteEditor.getObject();
        setWorldSize(10f);
        setCameraPos(0, 0);

        Notifications.registerObserver(this);

        mainRenderer = new MainRenderer();

        paletteEventPool = new Pool<PaletteEvent>() {
            @Override
            protected PaletteEvent newObject() {
                PaletteEvent e = new PaletteEvent();
                e.setTarget(PaletteEditorWorkspace.this);
                return e;
            }
        };

        gridProperties = new SceneEditorWorkspace.GridProperties();
        gridProperties.sizeProvider = new Supplier<float[]>() {
            @Override
            public float[] get () {
                if (SceneEditorWorkspace.getInstance().mapEditorState.isEditing()) {
                    TalosLayer selectedLayer = SceneEditorWorkspace.getInstance().mapEditorState.getLayerSelected();

                    if (selectedLayer == null) {
                        return new float[]{1,1};
                    } else {
                        return new float[]{selectedLayer.getTileSizeX(), selectedLayer.getTileSizeY()};
                    }
                }

                return new float[]{1, 1};

            }
        };

        gridDrawer = new GridDrawer(this, camera, gridProperties);
        gridDrawer.highlightCursorHover = true;
        gridDrawer.drawAxis = false;

        selectionRect = new Image(TalosMain.Instance().getSkin().getDrawable("orange_row"));
        selectionRect.setSize(0, 0);
        selectionRect.setVisible(false);
        addActor(selectionRect);

        initListeners();
    }

    private void initListeners () {
        inputListener = new InputListener() {
            // selection stuff
            Vector2 startPos = new Vector2();
            Vector2 vec = new Vector2();
            Rectangle rectangle = new Rectangle();
            boolean upWillClear = true;
            private boolean isSelectingWithDrag = false;

            // locals for registering click
            private long tapCountInterval = (long)(0.4f * 1000000000l);
            private int tapCount;
            private long lastTapTime;

            private Vector2 startOffsetFromParentTile = new Vector2();
            private boolean touchedDownOnEntityForParentDrag = false;

            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                if (canMoveAround) {
                    return false;
                }

                if (paletteEditor.isParentTileAndFakeHeightEditMode()) {
                    return false;
                }

                if (paletteEditor.isFreeTranslateEditMode()) {
                    if (selectedGameObject != null) {
                        if (entityUnderMouse == selectedGameObject) { //Setup translating if we are clicked on the entity we wanted
                            //Maybe we expand this later to include the parent tiles too

                            TileDataComponent tileDataComponent = selectedGameObject.getComponent(TileDataComponent.class);
                            GridPosition bottomLeftParentTile = tileDataComponent.getBottomLeftParentTile();

                            Vector2 worldFromLocal = getWorldFromLocal(x, y);
                            worldFromLocal.sub(bottomLeftParentTile.x, bottomLeftParentTile.y);

                            startOffsetFromParentTile.set(worldFromLocal.x, worldFromLocal.y);
                            touchedDownOnEntityForParentDrag = true;
                            return true;
                        }
                    }
                }

                paletteData.getResource().selectedGameAssets.clear();

                upWillClear = true;

                if (button == 2 || ctrlPressed()) {

                    isSelectingWithDrag = true;
                    selectionRect.setVisible(true);
                    selectionRect.setSize(0, 0);
                    startPos.set(x, y);

                    getStage().cancelTouchFocusExcept(this, PaletteEditorWorkspace.this);
                    event.handle();
                }

                return true;
            }

            @Override
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                if (canMoveAround) {
                    return;
                }

                if (paletteEditor.isParentTileAndFakeHeightEditMode()) {
                    return;
                }

                super.touchDragged(event, x, y, pointer);

                if (touchedDownOnEntityForParentDrag) {
                    //We are dragging, work out our current position, add it to the offset we stored, and snap it to the grid

                    Vector2 worldFromLocal = getWorldFromLocal(x, y);
                    worldFromLocal.sub(startOffsetFromParentTile);

                    TileDataComponent tileDataComponent = selectedGameObject.getComponent(TileDataComponent.class);
                    tileDataComponent.translateToWorldPosition(worldFromLocal);

                    return;
                }

                if(selectionRect.isVisible()) {
                    vec.set(x, y);
                    vec.sub(startPos);
                    if(vec.x < 0) {
                        rectangle.setX(x);
                    } else {
                        rectangle.setX(startPos.x);
                    }
                    if(vec.y < 0) {
                        rectangle.setY(y);
                    } else {
                        rectangle.setY(startPos.y);
                    }
                    rectangle.setWidth(Math.abs(vec.x));
                    rectangle.setHeight(Math.abs(vec.y));

                    selectionRect.setPosition(rectangle.x, rectangle.y);
                    selectionRect.setSize(rectangle.getWidth(), rectangle.getHeight());

                    event.handle();
                }

            }

            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                touchedDownOnEntityForParentDrag = false;

                if (canMoveAround) {
                    return;
                }

                if (paletteEditor.isParentTileAndFakeHeightEditMode()) {
                    return;
                }

                long time = TimeUtils.nanoTime();
                if (time - lastTapTime > tapCountInterval) tapCount = 0;
                tapCount++;
                lastTapTime = time;
                clicked(event, x, y);

                selectionRect.setVisible(false);
                isSelectingWithDrag = false;
            }

            public void clicked (InputEvent event, float x, float y) {
                if (tapCount == 1 && paletteEditor.getEditMode() == PaletteEditor.PaletteEditMode.NONE) {
                    //turn on the first edit mode if hitting asset
                    paletteEditor.startFreeTranslateEditMode();

                } else if (tapCount >= 2 && paletteEditor.isFreeTranslateEditMode()) {
                    //turn on the second edit mode if hitting assets
                    paletteEditor.endFreeTranslateEditMode();
                    paletteEditor.startFreeTransformEditMode();
                }

                if (!isSelectingWithDrag) {
                    // Find what we got on touch up and see
                    selectByPoint(x, y);
                }

                if (selectionRect.isVisible()) {
                    upWillClear = false;
                    selectByRect(rectangle);
                } else if(upWillClear) {
                    FocusManager.resetFocus(getStage());
                } else {
                    if (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                        // deselect all others, if they are selected
                        // if (deselectOthers(selectedGameObject)) { // TODO
                        // Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));
                        // }
                    }
                }
            }
        };

        addListener(new InputListener() {
            private boolean isDragging = false;
            private boolean overLine = false;

            final Vector2 dragStartPosition = new Vector2();

            private TileDataComponent tileDataComponent;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (!paletteEditor.isParentTileAndFakeHeightEditMode()) {
                    return false;
                }

                if (canMoveAround) {
                    return false;
                }

                tileDataComponent = selectedGameObject.getComponent(TileDataComponent.class);
                parentTilesReserve = tileDataComponent.getParentTiles();

                dragStartPosition.set(x, y);
                return true;
            }

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                if (!paletteEditor.isParentTileAndFakeHeightEditMode()) {
                    return false;
                }

                float x1, y1, x2, y2;
                float dist = 0.3f;
                float tmpDist;
                overLine = false;

                // check if hovering over line
                if (selectedGameObject != null) {
                    final Vector3 localPoint = new Vector3(x, y, 0);
                    getWorldFromLocal(localPoint);

                    // get x position
                    final TransformComponent transformComponent = selectedGameObject.getComponent(TransformComponent.class);
                    final TileDataComponent tileDataComponent = selectedGameObject.getComponent(TileDataComponent.class);
                    final GridPosition bottomLeftParentTile = tileDataComponent.getBottomLeftParentTile();
                    float xPos = bottomLeftParentTile.x + transformComponent.position.x;

                    x1 = xPos - 0.5f;
                    y1 = tmpHeightOffset;
                    x2 = xPos + 0.5f;
                    y2 = tmpHeightOffset;

                    tmpDist = Intersector.distanceLinePoint(x1, y1, x2, y2, localPoint.x, localPoint.y);

                    if (tmpDist <= dist) {
                        overLine = true;
                    }
                }

                return super.mouseMoved(event, x, y);
            }

            @Override
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                if (!paletteEditor.isParentTileAndFakeHeightEditMode()) {
                    return;
                }
                isDragging = true;
                if (overLine) { // line is selected, move it instead
                    Vector2 tmp = new Vector2(Gdx.input.getX(), Gdx.input.getY());
                    screenToLocalCoordinates(tmp);
                    tmp = getWorldFromLocal(tmp.x, tmp.y);
                    tmpHeightOffset = tmp.y;
                }
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (!paletteEditor.isParentTileAndFakeHeightEditMode()) {
                    return;
                }

                if (canMoveAround) {
                    return;
                }

                if (isDragging && paletteEditor.isParentTileAndFakeHeightEditMode() && !overLine) {
                    final Vector2 dragStartPos = new Vector2();
                    final Vector2 dragEndPos = new Vector2();

                    dragStartPos.x = dragStartPosition.x;
                    dragStartPos.y = dragStartPosition.y;

                    dragEndPos.x = x;
                    dragEndPos.y = y;

                    // convert to screen coordinates
                    localToScreenCoordinates(dragStartPos);
                    localToScreenCoordinates(dragEndPos);

                    // project to grid coordinates
                    gridDrawer.project(dragStartPos);
                    gridDrawer.project(dragEndPos);


                    final ObjectSet<GridPosition> parentTiles = new ObjectSet<>();

                    final int lowestX = (int) Math.min(dragStartPos.x, dragEndPos.x);
                    final int highestX = (int) Math.max(dragStartPos.x, dragEndPos.x);

                    final int lowestY = (int) Math.min(dragStartPos.y, dragEndPos.y);
                    final int highestY = (int) Math.max(dragStartPos.y, dragEndPos.y);

                    for (int i = lowestX; i <= highestX; i++) {
                        for (int j = lowestY; j <= highestY; j++) {
                            final GridPosition gridPosition = new GridPosition(i, j);
                            parentTiles.add(gridPosition);
                        }
                    }

                    tileDataComponent.setParentTiles(parentTiles);
                }

                super.touchUp(event, x, y, pointer, button);
                isDragging = false;
            }
        });

        addListener(inputListener);
    }

    private ObjectSet<GridPosition> parentTilesReserve;

    public void revertEditChanges () {
        final TileDataComponent tileDataComponent = selectedGameObject.getComponent(TileDataComponent.class);
        tileDataComponent.setParentTiles(parentTilesReserve);
    }

    @Override
    public void drawContent(Batch batch, float parentAlpha) {
        batch.end();
        gridDrawer.drawGrid();

        ObjectMap<UUID, float[]> positions = paletteData.getResource().positions;
        ObjectMap<GameAsset<?>, StaticTile> staticTiles = paletteData.getResource().staticTiles;
        OrderedMap<GameAsset<?>, GameObject> gameObjects = paletteData.getResource().gameObjects;


        //Sort the game objects
        gameObjects.orderedKeys().sort(gameObjectRenderOrderComparator);


        shapeRenderer.setProjectionMatrix(camera.combined);

        // render parent tiles
        for (ObjectMap.Entry<GameAsset<?>, GameObject> entry : gameObjects) {
            GameObject gameObject = entry.value;
            renderParentTiles(gameObject);
        }

        batch.begin();

        TalosLayer layerSelected = SceneEditorWorkspace.getInstance().mapEditorState.getLayerSelected();
        float tileSizeX = 1;
        float tileSizeY = 1;
        if (layerSelected != null) {
            tileSizeX = layerSelected.getTileSizeX();
            tileSizeY = layerSelected.getTileSizeY();
        }
        for (ObjectMap.Entry<GameAsset<?>, StaticTile> entry : staticTiles) {
            float[] pos = positions.get(entry.key.getRootRawAsset().metaData.uuid);

            StaticTile staticTile = entry.value;
            StaticTile value = staticTile;
            GridPosition gridPosition = value.getGridPosition();
            gridPosition.x = pos[0];
            gridPosition.y = pos[1];

            mainRenderer.renderStaticTileDynamic(staticTile, batch, tileSizeX, tileSizeY);
        }

        drawAllGameObjects(batch, gameObjects);

        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.CYAN);

        Array<GameAsset<?>> selectedGameAssets = paletteData.getResource().selectedGameAssets;

        for (ObjectMap.Entry<GameAsset<?>, StaticTile> entry : staticTiles) {
            GameAsset<?> key = entry.key;
            StaticTile value = entry.value;

            if (selectedGameAssets.contains(key, true)) {

                GridPosition gridPosition = value.getGridPosition();
                float gridSizeX = 1;
                float gridSizeY = 1;

                if (layerSelected != null) {
                    gridSizeX = layerSelected.getTileSizeX();
                    gridSizeY = layerSelected.getTileSizeY();
                }

                shapeRenderer.rect(gridPosition.getIntX(), gridPosition.getIntY(), gridSizeX, gridSizeY);
            }

        }

        if (paletteEditor.isParentTileAndFakeHeightEditMode()) {
            // draw the fake height lines
            if (selectedGameObject != null) {
                final TransformComponent transformComponent = selectedGameObject.getComponent(TransformComponent.class);
                final TileDataComponent tileDataComponent = selectedGameObject.getComponent(TileDataComponent.class);
                final GridPosition bottomLeftParentTile = tileDataComponent.getBottomLeftParentTile();

                float xPos = bottomLeftParentTile.x + transformComponent.position.x;

                shapeRenderer.line(
                        xPos - 0.5f, tmpHeightOffset,
                        xPos + 0.5f, tmpHeightOffset
                );
            }
        }

        shapeRenderer.end();


        beginEntitySelectionBuffer();
        drawEntitiesForSelection();
        endEntitySelectionBuffer();

        batch.begin();
    }

    private void renderParentTiles (GameObject gameObject) {
        // get tile data component if exists otherwise create
        final TileDataComponent tileDataComponent = gameObject.getComponent(TileDataComponent.class);

        // get grid size
        float gridSizeX = 1;
        float gridSizeY = 1;
        final TalosLayer layerSelected = SceneEditorWorkspace.getInstance().mapEditorState.getLayerSelected();
        if (layerSelected != null) {
            gridSizeX = layerSelected.getTileSizeX();
            gridSizeY = layerSelected.getTileSizeY();
        }

        // render rects
        Gdx.gl.glEnable(GL20.GL_BLEND);
        parentTilesColor.a = 0.5f;
        if (gameObject == selectedGameObject) {
            renderParentTilesHighlight(gameObject);
            parentTilesColor.a = 0.8f;
        }
        shapeRenderer.setColor(parentTilesColor);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (GridPosition parentTile : tileDataComponent.getParentTiles()) {
            shapeRenderer.rect(parentTile.x, parentTile.y, gridSizeX, gridSizeY);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void renderParentTilesHighlight (GameObject gameObject) {
        final TileDataComponent tileDataComponent = gameObject.getComponent(TileDataComponent.class);
        final ObjectSet<GridPosition> parentTiles = tileDataComponent.getParentTiles();

        // find the highest and lowest positions
        int firstX = parentTiles.first().getIntX();
        int firstY = parentTiles.first().getIntY();

        int lowestX = firstX;
        int highestX = firstX;
        int lowestY = firstY;
        int highestY = firstY;

        for (GridPosition parentTile : parentTiles) {
            if (parentTile.x < lowestX) lowestX = (int) parentTile.x;
            if (parentTile.y < lowestY) lowestY = (int) parentTile.y;
            if (parentTile.x > highestX) highestX = (int) parentTile.x;
            if (parentTile.y > highestY) highestY = (int) parentTile.y;
        }

        // get grid size
        float gridSizeX = 1;
        float gridSizeY = 1;
        final TalosLayer layerSelected = SceneEditorWorkspace.getInstance().mapEditorState.getLayerSelected();
        if (layerSelected != null) {
            gridSizeX = layerSelected.getTileSizeX();
            gridSizeY = layerSelected.getTileSizeY();
        }

        // render highlight
        Gdx.gl.glLineWidth(4f);
        shapeRenderer.setColor(1f, 1f, 1f, 1f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.rect(lowestX, lowestY, gridSizeX * (highestX - lowestX) + 1, gridSizeY * (highestY - lowestY) + 1);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1f);
    }

    @EventHandler
    public void onGameObjectSelectionChanged (GameObjectSelectionChanged event) {
        //Check if its this palettes object
        if (event.getContext() != this) return;

        Array<GameObject> gameObjects = event.get();
        if (!paletteEditor.isParentTileAndFakeHeightEditMode()) {
            selectGizmos(gameObjects);
        }
    }

    private Rectangle localParentTileCollider = new Rectangle();
    private void selectByPoint (float x, float y) {
        Vector3 localPoint = new Vector3(x, y, 0);
        getWorldFromLocal(localPoint);

        // get list of entities that have their origin in the rectangle
        ObjectMap<GameAsset<?>, StaticTile> staticTiles = paletteData.getResource().staticTiles;
        ObjectMap<UUID, float[]> positions = paletteData.getResource().positions;

        paletteData.getResource().selectedGameAssets.clear();

        PaletteEditor.PaletteFilterMode mode = PaletteEditor.PaletteFilterMode.NONE;
        GameObject activeGameObject = null;

        if (entityUnderMouse != null) {
            activeGameObject = entityUnderMouse;
            mode = PaletteEditor.PaletteFilterMode.ENTITY;
        }

        for (ObjectMap.Entry<GameAsset<?>, StaticTile> staticTileEntry : staticTiles) {
            GameAsset<?> gameAsset = staticTileEntry.key;
            UUID gameAssetUUID = gameAsset.getRootRawAsset().metaData.uuid;
            StaticTile staticTile = staticTileEntry.value;

            float[] pos = positions.get(gameAssetUUID);

            float minDistance = 1;
            float squaredDistance = minDistance * minDistance;
            float currentClosestDistance = squaredDistance + 1;

            float squareDistanceToCheck = (float)(Math.pow(pos[0] - localPoint.x, 2) + Math.pow(pos[1] - localPoint.y, 2));
            if (squareDistanceToCheck < squaredDistance) {
                if (squareDistanceToCheck < currentClosestDistance) {
                    mode = PaletteEditor.PaletteFilterMode.TILE;
                }
            }
        }

        if (mode != PaletteEditor.PaletteFilterMode.NONE) {
            PaletteEvent event = paletteEventPool.obtain();
            event.setType(PaletteEvent.Type.selected);
            event.setCurrentFilterMode(mode);

            if (mode == PaletteEditor.PaletteFilterMode.TILE) {
                notify(event, false);
            } else if (mode == PaletteEditor.PaletteFilterMode.ENTITY) {
                selectedGameObject = activeGameObject;

                SceneEditorWorkspace.getInstance().selectPropertyHolder(selectedGameObject);
                Array<GameObject> gameObjectSelection = new Array<>();
                gameObjectSelection.add(activeGameObject);
                Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(this, gameObjectSelection));
                notify(event, false);
            } else {
                paletteEventPool.free(event);
                selectedGameObject = null;

                Array<GameObject> gameObjectSelection = new Array<>();
                Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(this, gameObjectSelection));
            }
        } else {
            PaletteEvent event = paletteEventPool.obtain();
            event.setType(PaletteEvent.Type.lostFocus);
            event.setCurrentFilterMode(PaletteEditor.PaletteFilterMode.TILE_ENTITY);
            notify(event, false);
            selectedGameObject = null;

            Array<GameObject> gameObjectSelection = new Array<>();
            Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(this, gameObjectSelection));
        }
    }

    Vector3 lb = new Vector3();
    Vector3 lt = new Vector3();
    Vector3 rb = new Vector3();
    Vector3 rt = new Vector3();

    private void selectByRect(Rectangle rectangle) {
        Rectangle localRect = new Rectangle();
        lb.set(rectangle.x, rectangle.y, 0);
        lt.set(rectangle.x, rectangle.y + rectangle.height, 0);
        rb.set(rectangle.x + rectangle.width, rectangle.y, 0);
        rt.set(rectangle.x + rectangle.width, rectangle.y + rectangle.height, 0);
        getWorldFromLocal(lb);
        getWorldFromLocal(lt);
        getWorldFromLocal(rb);
        getWorldFromLocal(rt);
        localRect.set(lb.x, lb.y, Math.abs(rb.x - lb.x), Math.abs(lt.y - lb.y)); // selection rectangle in grid space

        // get list of entities that have their origin in the rectangle
        ObjectMap<GameAsset<?>, GameObject> gameObjects = paletteData.getResource().gameObjects;
        ObjectMap<GameAsset<?>, StaticTile> staticTiles = paletteData.getResource().staticTiles;
        ObjectMap<UUID, float[]> positions = paletteData.getResource().positions;

        paletteData.getResource().selectedGameAssets.clear();
        // entities
        for (ObjectMap.Entry<GameAsset<?>, GameObject> gameObjectEntry : gameObjects) {
            GameAsset<?> gameAsset = gameObjectEntry.key;
            GameObject gameObject = gameObjectEntry.value;
            TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
            if (localRect.contains(transformComponent.position)) {
                paletteData.getResource().selectedGameAssets.add(gameAsset);
            }
        }
        // static tiles
        for (ObjectMap.Entry<GameAsset<?>, StaticTile> staticTileEntry : staticTiles) {
            GameAsset<?> gameAsset = staticTileEntry.key;
            UUID gameAssetUUID = gameAsset.getRootRawAsset().metaData.uuid;
            float[] pos = positions.get(gameAssetUUID);
            if(localRect.contains(pos[0], pos[1])) {
                paletteData.getResource().selectedGameAssets.add(gameAsset);
            }
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        camera.update();
    }

    public void startEditMode () {
        TransformComponent transformComponent = selectedGameObject.getComponent(TransformComponent.class);
        TileDataComponent tileDataComponent = selectedGameObject.getComponent(TileDataComponent.class);
        tmpHeightOffset = tileDataComponent.getBottomLeftParentTile().y + transformComponent.position.y + tileDataComponent.getFakeZ();

        lockGizmos();
    }

    public float getTmpHeightOffset () {
        return tmpHeightOffset;
    }

    @Override
    protected InputListener addGizmoListener() {
        currentGizmoListener = super.addGizmoListener();
        return currentGizmoListener;
    }


    @Override
    protected void drawEntitiesForSelection () {
        super.drawEntitiesForSelection();

        mainRenderer.skipUpdates = true;

        PolygonSpriteBatchMultiTexture customBatch = entitySelectionBuffer.getCustomBatch();
        customBatch.setUsingCustomColourEncoding(true);
        customBatch.setProjectionMatrix(camera.combined);

        customBatch.disableBlending();
        customBatch.begin();

        OrderedMap<GameAsset<?>, GameObject> gameObjects = paletteData.getResource().gameObjects;

        drawAllGameObjects(customBatch, gameObjects);

        customBatch.end();

        mainRenderer.skipUpdates = false;

    }

    private void drawAllGameObjects (Batch batch, OrderedMap<GameAsset<?>, GameObject> gameObjects) {
        Array<GameAsset<?>> gameAssets = gameObjects.orderedKeys();
        for (GameAsset<?> gameAsset : gameAssets) {
            GameObject gameObject = gameObjects.get(gameAsset);

            //Calculate the position from parent tile bottom left + transform

            TransformComponent component = gameObject.getComponent(TransformComponent.class);
            Vector2 position = component.position;

            float storedX = position.x;
            float storedY = position.y;


            TileDataComponent tileDataComponent = gameObject.getComponent(TileDataComponent.class);
            GridPosition bottomLeftParentTile = tileDataComponent.getBottomLeftParentTile();

            gameObject.getTransformSettings().setOffset(bottomLeftParentTile.x, bottomLeftParentTile.y);


            position.set(storedX + bottomLeftParentTile.x, storedY + bottomLeftParentTile.y);

            //Set the transform to the temporary position which is bottom left parent tile + transform

            mainRenderer.update(gameObject);
            mainRenderer.render(batch, new MainRenderer.RenderState(), gameObject);

            position.set(storedX, storedY);
        }
    }

    @Override
    protected void getEntityUnderMouse () {
        Vector2 touchSpace = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        Vector2 uiSpace = screenToLocalCoordinates(touchSpace);

        uiSpace.x /= getWidth();
        uiSpace.y /= getHeight();

        Color color = entitySelectionBuffer.getPixelAtNDC(uiSpace);

        for (ObjectMap.Entry<GameAsset<?>, GameObject> entry : paletteData.getResource().gameObjects) {

            GameObject value = entry.value;

            GameObject entityForColourEncodedUUID = findEntityForColourEncodedUUID(color, value);
            if (entityForColourEncodedUUID != null) {
                entityUnderMouse = entityForColourEncodedUUID;
                return;
            }
        }

        entityUnderMouse = null;
    }

    public GameObject getSelectedGameObject() {
        return selectedGameObject;
    }
}
