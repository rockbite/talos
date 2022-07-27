package com.talosvfx.talos.editor.addons.scene.apps.tiledpalette;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.FocusManager;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectSelectionChanged;
import com.talosvfx.talos.editor.addons.scene.logic.Scene;
import com.talosvfx.talos.editor.addons.scene.logic.TilePaletteData;
import com.talosvfx.talos.editor.addons.scene.logic.components.MapComponent;
import com.talosvfx.talos.editor.addons.scene.maps.TalosLayer;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.Gizmo;
import com.talosvfx.talos.editor.notifications.Notifications;
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


    public PaletteEditorWorkspace(GameAsset<TilePaletteData> paletteData) {
        super();
        this.paletteData = paletteData;
        setWorldSize(10f);
        setCameraPos(0, 0);


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

                    getStage().setKeyboardFocus(PaletteEditorWorkspace.this);

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
                    //selectByRect(rectangle); todo
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

        for (ObjectMap.Entry<UUID, GameAsset<?>> reference : references) {
            float[] position = positions.get(reference.key);
            float x = position[0];
            float y = position[1];
            shapeRenderer.setColor(1, 0, 0, 1);
            shapeRenderer.rect(x, y, 1, 1);
        }
        shapeRenderer.end();

        batch.begin();
    }



    @Override
    public void act(float delta) {
        super.act(delta);
        camera.update();
    }
}
