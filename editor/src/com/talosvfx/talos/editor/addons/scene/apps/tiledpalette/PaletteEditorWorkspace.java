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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.*;
import com.kotcrab.vis.ui.FocusManager;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.TalosInputProcessor;
import com.talosvfx.talos.editor.addons.scene.MainRenderer;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectSelectionChanged;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.TilePaletteData;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TileDataComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.maps.GridPosition;
import com.talosvfx.talos.editor.addons.scene.maps.StaticTile;
import com.talosvfx.talos.editor.addons.scene.maps.TalosLayer;
import com.talosvfx.talos.editor.addons.scene.utils.PolygonSpriteBatchMultiTexture;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.utils.grid.property_providers.StaticGridPropertyProvider;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

import java.util.Comparator;


public class PaletteEditorWorkspace extends ViewportWidget implements Notifications.Observer {
    private PaletteEditor paletteEditor;
    GameAsset<TilePaletteData> paletteData;
    private Image selectionRect;

    private MainRenderer mainRenderer;

    private Pool<PaletteEvent> paletteEventPool;

    private float tmpHeightOffset;

    private boolean translatingMode = true;
    private ObjectMap<GameObject, Vector2> parentTileOffsetsForTranslate = new ObjectMap<>();

    private InputListener currentGizmoListener;

    // info for parent tile edit mode
    private Vector2 parentTileDragStart = new Vector2();
    private boolean parentTileIsDragging;

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

            boolean aISProxy = a instanceof TileGameObjectProxy;
            boolean bIsProxy = b instanceof TileGameObjectProxy;

            if (aISProxy && bIsProxy) return 0;
            if (aISProxy) return -1;
            if (bIsProxy) return 1;

            return orthoTopDownSorter.compare(a, b);
        }
    };

    private static final Color parentTilesColorBlue = Color.valueOf("#436787");
    private static final Color parentTilesBorderColorBlue = Color.valueOf("#6f9ac1");
    private static final Color parentTilesColorRed = Color.valueOf("#874343");
    private static final Color parentTilesBorderColorRed = Color.valueOf("#cc6a6a");
    private static final Color parentTilesProxyColor = ColorLibrary.ORANGE;
    private static final Color parentTileColorGray = Color.valueOf("#ffffff33");

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

        selectionRect = new Image(TalosMain.Instance().getSkin().getDrawable("orange_row"));
        selectionRect.setSize(0, 0);
        selectionRect.setVisible(false);
        addActor(selectionRect);
        addActor(rulerRenderer);

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

            private boolean touchedDownOnEntityForParentDrag = false;

            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                if (canMoveAround()) {
                    return false;
                }

                if (paletteEditor.isFreeTransformEditMode()) {
                    return false;
                }

                if (paletteEditor.isFakeHeightEditMode() || paletteEditor.isParentTileEditMode()) {
                    return false;
                }

                if (!isSelectingWithDrag) {
                    // Find what we got on touch up and see
                    selectByPoint(x, y);
                }

                if (paletteEditor.isFreeTranslateEditMode()) {
                    //If we have a selection and we are clicking on one of the selection members, we can start drag

                    if (selection.size > 0) {

                        //If we hit something with pixel
                        if (entityUnderMouse != null && selection.contains(entityUnderMouse)) {
                            setupSelectionForParentTileTranslation(x, y);
                            touchedDownOnEntityForParentDrag = true;
                            return true;
                        }

                        Vector2 worldFromLocal = getWorldFromLocal(x, y);

                        for (GameObject gameObject : selection) {
                            if (isPointOverGameObject(worldFromLocal, gameObject)) {
                                setupSelectionForParentTileTranslation(x, y);
                                touchedDownOnEntityForParentDrag = true;
                                return true;
                            }
                        }
                    }
                }


                upWillClear = true;

                if (button == 2 || TalosInputProcessor.ctrlPressed()) {

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
                if (canMoveAround()) {
                    return;
                }

                if (paletteEditor.isParentTileEditMode() || paletteEditor.isFakeHeightEditMode()) {
                    return;
                }

                super.touchDragged(event, x, y, pointer);

                if (touchedDownOnEntityForParentDrag) {
                    //We are dragging, work out our current position, add it to the offset we stored, and snap it to the grid

                    for (GameObject gameObject : selection) {
                        GameObject topParent = gameObject.getTopParent(paletteData.getResource().rootDummy);


                        Vector2 startOffsetFromParentTile = parentTileOffsetsForTranslate.get(topParent);

                        Vector2 worldFromLocal = getWorldFromLocal(x, y);
                        worldFromLocal.sub(startOffsetFromParentTile);


                        if (topParent instanceof TileGameObjectProxy) {
                            GridPosition bottomLeftParentTile = ((TileGameObjectProxy)topParent).staticTile.getGridPosition();

                            int newX = MathUtils.floor(worldFromLocal.x/1) * 1;
                            int newY = MathUtils.floor(worldFromLocal.y/1) * 1; //todo implement tile size

                            if (bottomLeftParentTile.x != newX || bottomLeftParentTile.y != newY) {
                                int deltaX = newX - bottomLeftParentTile.getIntX();
                                int deltaY = newY - bottomLeftParentTile.getIntY();

                                // lookout for future, do not directly change properties of grid position
                                bottomLeftParentTile.x += deltaX;
                                bottomLeftParentTile.y += deltaY;
                            }
                        } else {
                            TileDataComponent tileDataComponent = topParent.getComponent(TileDataComponent.class);
                            tileDataComponent.translateToWorldPosition(worldFromLocal);
                        }

                    }


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

            private GameObject entityClicked;
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                if (touchedDownOnEntityForParentDrag) {
                    touchedDownOnEntityForParentDrag = false;
                }


                if (canMoveAround()) {
                    return;
                }

                if (paletteEditor.isParentTileEditMode() || paletteEditor.isFakeHeightEditMode()) {
                    return;
                }

                // increase tap count only when tapping on the same thing
                long time = TimeUtils.nanoTime();
                if (time - lastTapTime > tapCountInterval) tapCount = 0;
                if (tapCount == 0 || entityClicked == entityUnderMouse) {
                    tapCount++;
                    entityClicked = entityUnderMouse;
                } else {
                    tapCount = 0;
                }

                lastTapTime = time;
                clicked(event, x, y);

                selectionRect.setVisible(false);
                isSelectingWithDrag = false;
            }

            public void clicked (InputEvent event, float x, float y) {
                if (tapCount == 1 && entityUnderMouse != null) {
                    PaletteEvent evn = paletteEventPool.obtain();
                    evn.setType(PaletteEvent.Type.startTranslate);
                    PaletteEditorWorkspace.this.notify(evn, false);
                } else if (tapCount >= 2 && paletteEditor.isFreeTranslateEditMode() && entityUnderMouse instanceof GameObject) {
                    PaletteEvent evn = paletteEventPool.obtain();
                    evn.setType(PaletteEvent.Type.startGizmoEdit);
                    PaletteEditorWorkspace.this.notify(evn, false);
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
            public boolean keyDown(InputEvent event, int keycode) {
                if (Gdx.input.isKeyPressed(Input.Keys.DEL) && !selection.isEmpty()) {
                    Array<GameAsset> markedForDeletion = new Array<>();
                    for (GameObject selectedGameObject : selection) {
                        for (ObjectMap.Entry<GameAsset<?>, GameObject> gameObject : paletteData.getResource().gameObjects) {
                            if (gameObject.value == selectedGameObject) {
                                markedForDeletion.add(gameObject.key);
                            }
                        }
                    }
                    for (GameAsset gameAsset : markedForDeletion) {
                        paletteEditor.removeEntity(gameAsset);
                    }
                    requestSelectionClear();
                }
                return super.keyDown(event, keycode);
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (!(paletteEditor.isParentTileEditMode() || paletteEditor.isFakeHeightEditMode())) {
                    return false;
                }

                if (canMoveAround()) {
                    return false;
                }

                tileDataComponent = getSelectedGameObject().getComponent(TileDataComponent.class);
                parentTilesReserve = tileDataComponent.getParentTiles();

                dragStartPosition.set(x, y);
                PaletteEditorWorkspace.this.parentTileDragStart.set(x, y);
                localToScreenCoordinates(parentTileDragStart);
                gridRenderer.project(parentTileDragStart);
                PaletteEditorWorkspace.this.parentTileIsDragging = true;
                return true;
            }

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                getStage().setKeyboardFocus(PaletteEditorWorkspace.this);

                if (!(paletteEditor.isFakeHeightEditMode() || paletteEditor.isParentTileEditMode())) {
                    return false;
                }

                float x1, y1, x2, y2;
                float dist = 0.3f;
                float tmpDist;
                overLine = false;

                // check if hovering over line
                if (getSelectedGameObject() != null) {
                    final Vector3 localPoint = new Vector3(x, y, 0);
                    getWorldFromLocal(localPoint);

                    // get x position
                    final TransformComponent transformComponent = getSelectedGameObject().getComponent(TransformComponent.class);
                    final TileDataComponent tileDataComponent = getSelectedGameObject().getComponent(TileDataComponent.class);
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
                if (!(paletteEditor.isFakeHeightEditMode() || paletteEditor.isParentTileEditMode())) {
                    return;
                }

                isDragging = true;
                if (paletteEditor.isFakeHeightEditMode() && overLine) { // line is selected, move it instead
                    Vector2 tmp = new Vector2(Gdx.input.getX(), Gdx.input.getY());
                    screenToLocalCoordinates(tmp);
                    tmp = getWorldFromLocal(tmp.x, tmp.y);
                    tmpHeightOffset = tmp.y;
                }
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                PaletteEditorWorkspace.this.parentTileIsDragging = false;
                if (!(paletteEditor.isParentTileEditMode() || paletteEditor.isFakeHeightEditMode())) {
                    return;
                }

                if (canMoveAround()) {
                    return;
                }

                if (paletteEditor.isParentTileEditMode() && isDragging) {
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
                    gridRenderer.project(dragStartPos);
                    gridRenderer.project(dragEndPos);

                    final ObjectSet<GridPosition> parentTiles = tileDataComponent.getParentTiles();
                    final GridPosition dummyCell = new GridPosition(dragStartPos.x, dragStartPos.y);
                    boolean shouldAppend = !parentTiles.contains(dummyCell);

                    final int lowestX = (int) Math.min(dragStartPos.x, dragEndPos.x);
                    final int highestX = (int) Math.max(dragStartPos.x, dragEndPos.x);

                    final int lowestY = (int) Math.min(dragStartPos.y, dragEndPos.y);
                    final int highestY = (int) Math.max(dragStartPos.y, dragEndPos.y);

                    for (int i = highestX; i >= lowestX; i--) {
                        for (int j = highestY; j >= lowestY; j--) {
                            final GridPosition gridPosition = new GridPosition(i, j);
                            if (shouldAppend) {
                                if (!parentTiles.contains(gridPosition)) {
                                    parentTiles.add(gridPosition);
                                }
                            } else { // remove intersecting cells
                                if (parentTiles.size > 1 && parentTiles.contains(gridPosition)) {
                                    parentTiles.remove(gridPosition);
                                }
                            }
                        }
                    }
                } else if (paletteEditor.isParentTileEditMode() && !isDragging) {
                    final Vector2 touchPos = new Vector2(x, y);

                    // convert to screen coordinates
                    localToScreenCoordinates(touchPos);

                    // project to grid coordinates
                    gridRenderer.project(touchPos);

                    ObjectSet<GridPosition> parentTiles = tileDataComponent.getParentTiles();
                    final GridPosition gridPosition = new GridPosition(touchPos.x, touchPos.y);
                    GridPosition existingGridPosition = parentTiles.get(gridPosition);
                    if (existingGridPosition == null) {
                        parentTiles.add(gridPosition);
                    } else if (existingGridPosition != null && parentTiles.size > 1) {
                        parentTiles.remove(existingGridPosition);
                    }
                } else if (paletteEditor.isFakeHeightEditMode() && isDragging) {
                    GameObject gameObject = getSelectedGameObject();
                    TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
                    TileDataComponent tileDataComponent = gameObject.getComponent(TileDataComponent.class);
                    tileDataComponent.setFakeZ(tmpHeightOffset - (tileDataComponent.getBottomLeftParentTile().y + transformComponent.position.y));
                    AssetRepository.getInstance().saveGameAssetResourceJsonToFile(paletteEditor.getObject());
                }

                super.touchUp(event, x, y, pointer, button);
                isDragging = false;
            }
        });

        addListener(inputListener);
    }

    private void setupSelectionForParentTileTranslation (float localX, float localY) {
        Vector2 worldFromLocal = getWorldFromLocal(localX, localY);

        for (GameObject gameObject : selection) {
            GameObject topParent = gameObject.getTopParent(paletteData.getResource().rootDummy);
            TileDataComponent tileDataComponent = topParent.getComponent(TileDataComponent.class);

            Vector2 vector2 = new Vector2();
            vector2.set(worldFromLocal);

            if (gameObject instanceof TileGameObjectProxy) {
                StaticTile staticTile = ((TileGameObjectProxy)gameObject).staticTile;
                GridPosition gridPosition = staticTile.getGridPosition();
                vector2.sub(gridPosition.x, gridPosition.y);
            } else {
                GridPosition bottomLeftParentTile = tileDataComponent.getBottomLeftParentTile();
                vector2.sub(bottomLeftParentTile.x, bottomLeftParentTile.y);
            }

            parentTileOffsetsForTranslate.put(topParent, vector2);

        }
    }

    private ObjectSet<GridPosition> parentTilesReserve;

    public void revertEditChanges () {
        final TileDataComponent tileDataComponent = getSelectedGameObject().getComponent(TileDataComponent.class);
        tileDataComponent.setParentTiles(parentTilesReserve);
    }

    @Override
    public void drawContent(Batch batch, float parentAlpha) {
        batch.end();
        gridPropertyProvider.setLineThickness(pixelToWorld(1.2f));
        gridPropertyProvider.update(camera, parentAlpha);
        gridRenderer.drawGrid(batch, shapeRenderer);

        OrderedMap<GameAsset<?>, GameObject> gameObjects = paletteData.getResource().gameObjects;


        //Sort the game objects
        gameObjects.orderedKeys().sort(gameObjectRenderOrderComparator);


        shapeRenderer.setProjectionMatrix(camera.combined);

        // render parent tiles
        for (ObjectMap.Entry<GameAsset<?>, GameObject> entry : gameObjects) {
            GameObject gameObject = entry.value;

            GameObject topLevelParent = gameObject.getTopParent(paletteData.getResource().rootDummy);
            renderParentTiles(topLevelParent);
        }

        if (paletteEditor.isParentTileEditMode()) {
            Vector2 pointer = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            screenToLocalCoordinates(pointer);
            pointer = getWorldFromLocal(pointer.x, pointer.y);

            pointer.x = pointer.x > 0 ? (int) pointer.x : -1 + (int) pointer.x;
            pointer.y = pointer.y > 0 ? (int) pointer.y : -1 + (int) pointer.y;

            int lowestX, highestX;
            int lowestY, highestY;

            if (parentTileIsDragging) {
                lowestX = (int) Math.min(parentTileDragStart.x, pointer.x);
                highestX = (int) Math.max(parentTileDragStart.x, pointer.x);

                lowestY = (int) Math.min(parentTileDragStart.y, pointer.y);
                highestY = (int) Math.max(parentTileDragStart.y, pointer.y);
            } else {
                lowestX = (int) pointer.x;
                highestX = (int) pointer.x;

                lowestY = (int) pointer.y;
                highestY = (int) pointer.y;
            }

            // render rects
            ObjectSet<GridPosition> parentTiles = getSelectedGameObject().getComponent(TileDataComponent.class).getParentTiles();
            GridPosition dummyCell;
            if (parentTileIsDragging) {
                dummyCell = new GridPosition(parentTileDragStart.x, parentTileDragStart.y);
            } else {
                dummyCell = new GridPosition(pointer.x, pointer.y);
            }

            boolean shouldAppend = !parentTiles.contains(dummyCell);
            if (shouldAppend) {
                shapeRenderer.setColor(parentTilesColorBlue);
            } else {
                shapeRenderer.setColor(parentTilesColorRed);
            }
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            for (int i = lowestX; i <= highestX; i++) {
                for (int j = lowestY; j <= highestY; j++) {
                    shapeRenderer.rect(i, j, 1, 1);
                }
            }

            shapeRenderer.end();
            if (shouldAppend) {
                shapeRenderer.setColor(parentTilesBorderColorBlue);
            } else {
                shapeRenderer.setColor(parentTilesBorderColorRed);
            }
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            for (int i = lowestX; i <= highestX; i++) {
                for (int j = lowestY; j <= highestY; j++) {
                    shapeRenderer.rect(i, j, 1, 1);
                }
            }
            shapeRenderer.end();

            batch.begin();
            Skin skin = TalosMain.Instance().getSkin();
            Drawable appendIcon = skin.getDrawable("tile-plus-icon");
            Drawable removeIcon = skin.getDrawable("tile-minus-icon");
            float totalScreenSpaceParentSize = getWidth();
            float totalWorldWidth = getWorldWidth() * camera.zoom;
            float worldPerPixel = totalWorldWidth / totalScreenSpaceParentSize;
            float icWidth = worldPerPixel * 12f;
            float icHeight = worldPerPixel * 12f;
            Drawable icon;
            if (shouldAppend) {
                icon = appendIcon;
            } else {
                icon = removeIcon;
            }
            for (int i = lowestX; i <= highestX; i++) {
                for (int j = lowestY; j <= highestY; j++) {
                    icon.draw(batch,i + 0.5f - icWidth / 2f, j  + 0.5f - icHeight / 2f, icWidth, icHeight);
                }
            }
            batch.end();
        }


        batch.begin();

        TalosLayer layerSelected = SceneEditorWorkspace.getInstance().mapEditorState.getLayerSelected();
        float tileSizeX = 1;
        float tileSizeY = 1;
        if (layerSelected != null) {
            tileSizeX = layerSelected.getTileSizeX();
            tileSizeY = layerSelected.getTileSizeY();
        }

        for (ObjectMap.Entry<GameAsset<?>, GameObject> entry : gameObjects) {
            if (entry.value instanceof TileGameObjectProxy) {

                TileGameObjectProxy value = (TileGameObjectProxy)entry.value;
                StaticTile staticTile = value.staticTile;

                mainRenderer.renderStaticTileDynamic(staticTile, batch, tileSizeX, tileSizeY);
            }
        }


        drawAllGameObjects(batch, gameObjects);

        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(ColorLibrary.BORDER_BLUE);


//        for (ObjectMap.Entry<GameAsset<?>, StaticTile> entry : staticTiles) {
//            Gdx.gl.glLineWidth(3);
//            GameAsset<?> key = entry.key;
//            StaticTile value = entry.value;
//
//            if (true) {
//
//                GridPosition gridPosition = value.getGridPosition();
//                float gridSizeX = 1;
//                float gridSizeY = 1;
//
//                if (layerSelected != null) {
//                    gridSizeX = layerSelected.getTileSizeX();
//                    gridSizeY = layerSelected.getTileSizeY();
//                }
//
//                shapeRenderer.rect(gridPosition.getIntX(), gridPosition.getIntY(), gridSizeX, gridSizeY);
//            }
//
//        }

        if (paletteEditor.isFakeHeightEditMode()) {
            // draw the fake height lines
            if (getSelectedGameObject()!= null) {
                final TransformComponent transformComponent = getSelectedGameObject().getComponent(TransformComponent.class);
                final TileDataComponent tileDataComponent = getSelectedGameObject().getComponent(TileDataComponent.class);
                final GridPosition bottomLeftParentTile = tileDataComponent.getBottomLeftParentTile();

                float xPos = bottomLeftParentTile.x + transformComponent.position.x;

                float width = 2f; // grid space

                if (getSelectedGameObject().hasComponent(SpriteRendererComponent.class)) {
                    SpriteRendererComponent spriteRendererComponent = getSelectedGameObject().getComponent(SpriteRendererComponent.class);
                    width = spriteRendererComponent.size.x + 1f;
                }

                shapeRenderer.line(
                        xPos - width / 2f, tmpHeightOffset,
                        xPos + width / 2f, tmpHeightOffset
                );

                shapeRenderer.end();
                batch.begin();
                Skin skin = TalosMain.Instance().getSkin();
                Drawable lineAdjustIcon = skin.getDrawable("adjust-line-icon");
                float totalScreenSpaceParentSize = getWidth();
                float totalWorldWidth = getWorldWidth() * camera.zoom;
                float worldPerPixel = totalWorldWidth / totalScreenSpaceParentSize;
                float icWidth = worldPerPixel * 8f;
                float icHeight = worldPerPixel * 16f;
                lineAdjustIcon.draw(batch, xPos - width / 2f, tmpHeightOffset - icHeight / 2f, icWidth, icHeight);
                lineAdjustIcon.draw(batch, xPos + width / 2f - icWidth / 2f, tmpHeightOffset - icHeight / 2f, icWidth, icHeight);
                batch.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
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

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Color renderColor =  parentTileColorGray;

        if (selection.contains(gameObject)) {
            renderColor = gameObject instanceof TileGameObjectProxy ? parentTilesProxyColor : parentTilesColorBlue;
        }


        shapeRenderer.setColor(renderColor);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (GridPosition parentTile : tileDataComponent.getParentTiles()) {
            shapeRenderer.rect(parentTile.x, parentTile.y, gridSizeX, gridSizeY);
        }
        shapeRenderer.end();

        Gdx.gl.glLineWidth(2f);
        if (selection.contains(gameObject)) {
            Color renderBorderColor = gameObject instanceof TileGameObjectProxy ? parentTilesProxyColor : parentTilesBorderColorBlue;

            shapeRenderer.setColor(renderBorderColor);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            for (GridPosition parentTile : tileDataComponent.getParentTiles()) {
                shapeRenderer.rect(parentTile.x, parentTile.y, gridSizeX, gridSizeY);
            }

            shapeRenderer.end();
        }
        Gdx.gl.glLineWidth(1f);
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

        if (event.get().size == 0) {
            PaletteEvent e = paletteEventPool.obtain();
            e.setType(PaletteEvent.Type.lostFocus);
            notify(e, false);
        }

        ObjectSet<GameObject> gameObjects = event.get();

        if (!(paletteEditor.isFakeHeightEditMode() || paletteEditor.isParentTileEditMode())) {
            selectGizmos(gameObjects);
        }
    }

    private void selectByPoint (float x, float y) {
        Vector3 localPoint = new Vector3(x, y, 0);
        getWorldFromLocal(localPoint);

        // get list of entities that have their origin in the rectangle
        ObjectMap<GameAsset<?>, GameObject> gameObjects = paletteData.getResource().gameObjects;

        GameObject activeGameObject = null;

        if (entityUnderMouse != null) {
            activeGameObject = entityUnderMouse;
        }

        if (entityUnderMouse == null) {
            for (ObjectMap.Entry<GameAsset<?>, GameObject> gameObjectEntry : gameObjects) {
                if (gameObjectEntry.value instanceof TileGameObjectProxy) {
                    GameObject gameObject = gameObjectEntry.value;

                    if (((TileGameObjectProxy)gameObject).containsPoint(new Vector2(localPoint.x, localPoint.y))) {
                        Array<GameObject> selectionObjects = new Array<>();
                        selectionObjects.add(gameObject);
                        setSelection(selectionObjects);

                        PaletteEvent event = paletteEventPool.obtain();
                        event.setType(PaletteEvent.Type.selected);
                        event.setSelectedGameObjects(selectionObjects.toArray(TileGameObjectProxy.class));
                        notify(event, false);
                        return;
                    }
                }
            }
        }

        if (activeGameObject != null) {
            PaletteEvent event = paletteEventPool.obtain();
            event.setType(PaletteEvent.Type.selected);

            Array<GameObject> selectionObjects = new Array<>();
            selectionObjects.add(activeGameObject);
            setSelection(selectionObjects);

            notify(event, false);
        } else {
            requestSelectionClear();
        }

    }

    @Override
    protected void setSelection (Array<GameObject> gameObjects) {
        //Intercept
        Array<GameObject> toppest = new Array<>();
        for (GameObject gameObject : gameObjects) {
            toppest.add(gameObject.getTopParent(paletteData.getResource().rootDummy));
        }
        super.setSelection(toppest);
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

        requestSelectionClear();
        // entities
        for (ObjectMap.Entry<GameAsset<?>, GameObject> gameObjectEntry : gameObjects) {
            GameAsset<?> gameAsset = gameObjectEntry.key;
            GameObject gameObject = gameObjectEntry.value;

            if (isGameObjectInsideRect(localRect, gameObject)) {
                addToSelection(gameObject);

            }

        }
    }

    private boolean isPointOverGameObject (Vector2 worldPos, GameObject gameObject) {
        if (gameObject instanceof TileGameObjectProxy) {
            return ((TileGameObjectProxy)gameObject).containsPoint(worldPos);
        }


        //Check prefab parents
        GameObject topLevelParent = gameObject.getTopParent(paletteData.getResource().rootDummy);


        //Use the top level one
        TransformComponent transformComponent = topLevelParent.getComponent(TransformComponent.class);
        TileDataComponent tileDataComponent = topLevelParent.getComponent(TileDataComponent.class);


        Rectangle tempVec = new Rectangle();
        ObjectSet<GridPosition> parentTiles = tileDataComponent.getParentTiles();
        for (GridPosition parentTile : parentTiles) {
            tempVec.set(parentTile.x, parentTile.y, 1, 1);
            if (tempVec.contains(worldPos)) {
                return true;
            }
        }

        if (gameObject.hasComponent(SpriteRendererComponent.class)) {
            SpriteRendererComponent spriteRendererComponent = gameObject.getComponent(SpriteRendererComponent.class);
            GridPosition bottomLeftParentTile = tileDataComponent.getBottomLeftParentTile();
            float offsetX = bottomLeftParentTile.x;
            float offsetY = bottomLeftParentTile.y;

            float transformX = offsetX + transformComponent.position.x;
            float transformY = offsetY + transformComponent.position.y;

            Vector2 size = spriteRendererComponent.size;
            tempVec.set(transformX - size.x/2, transformY - size.y/2, size.x, size.y);
            if (tempVec.contains(worldPos)) {
                return true;
            }
        }
        return false;
    }

    private boolean isGameObjectInsideRect (Rectangle localRect, GameObject gameObject) {
        if (gameObject instanceof TileGameObjectProxy) {
            StaticTile staticTile = ((TileGameObjectProxy)gameObject).staticTile;
            GridPosition gridPosition = staticTile.getGridPosition();
            return localRect.contains(gridPosition.x, gridPosition.getIntY());
        }

        TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
        TileDataComponent tileDataComponent = gameObject.getComponent(TileDataComponent.class);

        //Check fro parent tiles inside selection, or transform, or even bounds


        //Check any of the parent tiles
        for (GridPosition parentTile : tileDataComponent.getParentTiles()) {
            if (localRect.contains(parentTile.x, parentTile.y)) {
                return true;
            }
        }

        GridPosition bottomLeftParentTile = tileDataComponent.getBottomLeftParentTile();
        float offsetX = bottomLeftParentTile.x;
        float offsetY = bottomLeftParentTile.y;

        float transformX = offsetX + transformComponent.position.x;
        float transformY = offsetY + transformComponent.position.y;

        if (localRect.contains(transformX, transformY)) {
            return true;
        }

        if (gameObject.hasComponent(SpriteRendererComponent.class)) {
            //We can also check its sprite bounds
            SpriteRendererComponent spriteRendererComponent = gameObject.getComponent(SpriteRendererComponent.class);
            Vector2 size = spriteRendererComponent.size;

            if (localRect.contains(transformX - size.x/2, transformY - size.y/2)) {
                return true;
            }
            if (localRect.contains(transformX - size.x/2, transformY + size.y/2)) {
                return true;
            }
            if (localRect.contains(transformX + size.x/2, transformY - size.y/2)) {
                return true;
            }
            if (localRect.contains(transformX + size.x/2, transformY + size.y/2)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        camera.update();
    }

    public void startFakeHeightEditMode () {
        TransformComponent transformComponent = getSelectedGameObject().getComponent(TransformComponent.class);
        TileDataComponent tileDataComponent = getSelectedGameObject().getComponent(TileDataComponent.class);
        tmpHeightOffset = tileDataComponent.getBottomLeftParentTile().y + transformComponent.position.y + tileDataComponent.getFakeZ();

        lockGizmos();
    }

    @Override
    protected InputListener addGizmoListener() {
        currentGizmoListener = super.addGizmoListener();
        return currentGizmoListener;
    }


    @Override
    protected void drawEntitiesForSelection () {
        super.drawEntitiesForSelection();

        mainRenderer.setRenderingEntitySelectionBuffer(true);

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


        mainRenderer.setRenderingEntitySelectionBuffer(false);

    }

    @Override
    public void initializeGridPropertyProvider () {
        gridPropertyProvider = new StaticGridPropertyProvider();
        gridPropertyProvider.getBackgroundColor().set(Color.valueOf("#272727"));
    }

    private void drawAllGameObjects (Batch batch, OrderedMap<GameAsset<?>, GameObject> gameObjects) {
        Array<GameAsset<?>> gameAssets = gameObjects.orderedKeys();
        for (GameAsset<?> gameAsset : gameAssets) {
            GameObject gameObject = gameObjects.get(gameAsset);

            if (gameObject instanceof TileGameObjectProxy) continue;

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
        if (selection.size == 0) return null;
        return selection.orderedItems().first();
    }
}
