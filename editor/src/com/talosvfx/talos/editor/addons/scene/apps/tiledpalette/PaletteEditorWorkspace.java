package com.talosvfx.talos.editor.addons.scene.apps.tiledpalette;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.FocusManager;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.MainRenderer;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.TilePaletteData;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.maps.GridPosition;
import com.talosvfx.talos.editor.addons.scene.maps.StaticTile;
import com.talosvfx.talos.editor.addons.scene.maps.TalosLayer;
import com.talosvfx.talos.editor.utils.GridDrawer;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;

import java.util.UUID;
import java.util.function.Supplier;

import static com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace.ctrlPressed;

public class PaletteEditorWorkspace extends ViewportWidget {
    GameAsset<TilePaletteData> paletteData;


    private GridDrawer gridDrawer;
    private SceneEditorWorkspace.GridProperties gridProperties;

    private Image selectionRect;

    private MainRenderer mainRenderer;

    public PaletteEditorWorkspace(GameAsset<TilePaletteData> paletteData) {
        super();
        this.paletteData = paletteData;
        setWorldSize(10f);
        setCameraPos(0, 0);

        mainRenderer = new MainRenderer();

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

        selectionRect = new Image(TalosMain.Instance().getSkin().getDrawable("orange_row"));
        selectionRect.setSize(0, 0);
        selectionRect.setVisible(false);
        addActor(selectionRect);

        addListener(new InputListener() {

            // selection stuff
            Vector2 startPos = new Vector2();
            Vector2 vec = new Vector2();
            Rectangle rectangle = new Rectangle();
            boolean upWillClear = true;

            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {

                upWillClear = true;


                if(button == 2 || ctrlPressed()) {

                    selectionRect.setVisible(true);
                    selectionRect.setSize(0, 0);
                    startPos.set(x, y);

                    getStage().cancelTouchFocusExcept(this, PaletteEditorWorkspace.this);


                    event.handle();


                    return true;
                }
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);

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

                if(selectionRect.isVisible()) {
                    upWillClear = false;
                    selectByRect(rectangle);
//                    Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection)); //todo
                } else if(upWillClear) {
                    FocusManager.resetFocus(getStage());
//                    clearSelection(); todo
//                    Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));
                } else {
                    if(!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                        // deselect all others, if they are selected
//                        if(deselectOthers(selectedGameObject)) {/ /todo
//                            Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));
//                        }
                    }
                }


                selectionRect.setVisible(false);

                super.touchUp(event, x, y, pointer, button);
            }
        });
    }

    @Override
    public void drawContent(Batch batch, float parentAlpha) {
        batch.end();

        gridDrawer.drawGrid();

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        ObjectMap<UUID, float[]> positions = paletteData.getResource().positions;
        ObjectMap<UUID, GameAsset<?>> references = paletteData.getResource().references;

        ObjectMap<GameAsset<?>, StaticTile> staticTiles = paletteData.getResource().staticTiles;
        ObjectMap<GameAsset<?>, GameObject> gameObjects = paletteData.getResource().gameObjects;


        for (ObjectMap.Entry<UUID, GameAsset<?>> reference : references) {
            float[] position = positions.get(reference.key);
            float x = position[0];
            float y = position[1];
            shapeRenderer.setColor(1, 0, 0, 1);
            shapeRenderer.rect(x, y, 1, 1);
        }
        shapeRenderer.end();

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
        for (ObjectMap.Entry<GameAsset<?>, GameObject> entry : gameObjects) {
            float[] pos = positions.get(entry.key.getRootRawAsset().metaData.uuid);

            GameObject gameObject = entry.value;
            if (gameObject.hasComponent(TransformComponent.class)) {
                TransformComponent transform = gameObject.getComponent(TransformComponent.class);
                transform.position.set(pos[0], pos[1]);
            }

            mainRenderer.update(gameObject);
            mainRenderer.render(batch, gameObject);
        }
    }

    public void selectByRect(Rectangle rectangle) {
        Rectangle localRect = new Rectangle();
        Vector3 lb = new Vector3(rectangle.x, rectangle.y, 0);
        Vector3 lt = new Vector3(rectangle.x, rectangle.y + rectangle.height, 0);
        Vector3 rb = new Vector3(rectangle.x + rectangle.width, rectangle.y, 0);
        Vector3 rt = new Vector3(rectangle.x + rectangle.width, rectangle.y + rectangle.height, 0);
        PaletteEditorWorkspace.this.getWorldFromLocal(lb);
        PaletteEditorWorkspace.this.getWorldFromLocal(lt);
        PaletteEditorWorkspace.this.getWorldFromLocal(rb);
        PaletteEditorWorkspace.this.getWorldFromLocal(rt);
        localRect.set(lb.x, lb.y, Math.abs(rb.x - lb.x), Math.abs(lt.y - lb.y)); // selection rectangle in grid space

        // get list of entities that have their origin in the rectangle
        ObjectMap<GameAsset<?>, GameObject> gameObjects = paletteData.getResource().gameObjects;
        ObjectMap<UUID, float[]> positions = paletteData.getResource().positions;

        for (ObjectMap.Entry<GameAsset<?>, GameObject> gameObject : gameObjects) {
            float[] pos = positions.get(gameObject.key.getRootRawAsset().metaData.uuid);
            if(localRect.contains(pos[0], pos[1])) {
                System.out.println("Contains the element with uid " + gameObject.key.getRootRawAsset().metaData.uuid);
            }
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        camera.update();
    }
}
