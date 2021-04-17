package com.talosvfx.talos.editor.addons.uieditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;

public class UIWorkspace extends ViewportWidget implements Json.Serializable {

    private final UIAddon uiAddon;

    private Stage stage;

    private Image canvasBackground;
    private Group canvasContainer;

    private Color canvasBg = new Color(0x282728ff);

    private Vector2 tmpVec = new Vector2();
    private Vector3 vec3 = new Vector3();

    private Array<BoxTransform> transformArray = new Array<>();

    public UIWorkspace (UIAddon uiAddon) {
        this.uiAddon = uiAddon;

        Pools.set(BoxTransform.class, new Pool<BoxTransform>() {
            @Override
            protected BoxTransform newObject () {
                return new BoxTransform();
            }
        });

        setWorldSize(1280);
        setSkin(TalosMain.Instance().getSkin());

        setCameraPos(0, 0);
        bgColor.set(0.1f, 0.1f, 0.1f, 1f);

        clearListeners();

        stage = new Stage();
        stage.getViewport().setCamera(camera);

        initCanvas();

        addListeners();
        addPanListener();
    }

    private void initCanvas () {
        canvasBackground = new Image(getSkin().getDrawable("white"));
        canvasBackground.setColor(canvasBg);

        canvasContainer = new Group();
        canvasBackground.setTouchable(Touchable.enabled);
        canvasContainer.setTouchable(Touchable.disabled);
        canvasContainer.setTransform(false);
        canvasContainer.setSize(511, 511);

        stage.addActor(canvasBackground);
        stage.addActor(canvasContainer);

        canvasBackground.setPosition(camera.position.x - canvasContainer.getWidth()/2f, camera.position.y - canvasContainer.getHeight()/2f);
        canvasContainer.setPosition(canvasBackground.getX(), canvasBackground.getY());

        Table table = new Table();
        table.setSize(511, 511);
        canvasContainer.addActor(table);
    }

    private void addListeners () {
        addListener(new InputListener() {

            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                boolean parent = super.touchDown(event, x, y, pointer, button);

                tmpVec.set(x, y);
                toContentPosition(tmpVec);

                boolean hitDetected = false;
                for (BoxTransform box: transformArray) {
                    boolean hit = box.touchDown(tmpVec.x,  tmpVec.y);
                    if(hit) {
                        hitDetected = true;
                    }
                }

                if (hitDetected) {
                    event.handle();
                    return true;
                }

                // we clicked somewhere empty?
                if(!parent) {
                    vec3.set(tmpVec.x, tmpVec.y, 0);
                    camera.project(vec3);
                    parent = stage.touchDown((int)vec3.x, Gdx.graphics.getHeight() - (int)vec3.y, pointer, button);

                    if(!parent) {
                        unSelectAll();
                    }
                }

                return parent;
            }

            @Override
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);

                tmpVec.set(x, y);
                toContentPosition(tmpVec);

                for (BoxTransform box: transformArray) {
                    box.touchDragged(tmpVec.x,  tmpVec.y);
                }
            }

            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);

                tmpVec.set(x, y);
                toContentPosition(tmpVec);

                for (BoxTransform box: transformArray) {
                    box.touchUp(tmpVec.x,  tmpVec.y);
                }

                vec3.set(tmpVec.x, tmpVec.y, 0);
                camera.project(vec3);
                stage.touchUp((int)vec3.x, Gdx.graphics.getHeight() - (int)vec3.y, pointer, button);
            }
        });

        canvasBackground.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);

                selectActor(canvasContainer);
            }
        });
    }

    private void selectActor(Actor actor) {
        for(BoxTransform boxTransform: transformArray) {
            if(boxTransform.getActor() != actor) {
                Pools.free(boxTransform);
            } else {
                transformArray.clear();
                transformArray.add(boxTransform);
                return;
            }
        }

        transformArray.clear();

        BoxTransform boxTransform = Pools.obtain(BoxTransform.class);
        boxTransform.setWorkspace(UIWorkspace.this);
        boxTransform.setActor(actor);
        transformArray.add(boxTransform);
    }

    private void unSelectAll() {
        for(BoxTransform boxTransform: transformArray) {
            Pools.free(boxTransform);
        }

        transformArray.clear();
    }



    @Override
    public void write (Json json) {

    }

    @Override
    public void read (Json json, JsonValue jsonData) {

    }

    @Override
    public void act (float delta) {
        super.act(delta);
        stage.act();

        canvasBackground.setPosition(canvasContainer.getX(), canvasContainer.getY());
        canvasBackground.setSize(canvasContainer.getWidth(), canvasContainer.getHeight());
    }

    @Override
    public void drawContent (Batch batch, float parentAlpha) {
        batch.end();
        drawGrid(batch, parentAlpha);
        batch.begin();

        stage.getBatch().setProjectionMatrix(batch.getProjectionMatrix());
        stage.getBatch().setTransformMatrix(batch.getTransformMatrix());
        stage.getRoot().draw(batch, parentAlpha);

        drawTools(batch, parentAlpha);
    }

    private void drawTools(Batch batch, float parentAlpha) {
        batch.end();

        Gdx.gl.glLineWidth(1f);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 0; i < transformArray.size; i++) {
            transformArray.get(i).draw(shapeRenderer);
        }

        shapeRenderer.end();
        batch.begin();
    }

    public Stage getMainStage () {
        return stage;
    }
}
