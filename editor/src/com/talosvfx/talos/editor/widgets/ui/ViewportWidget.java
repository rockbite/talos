/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.talosvfx.talos.editor.widgets.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.GameObjectContainer;
import com.talosvfx.talos.editor.addons.scene.logic.components.AComponent;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.Gizmo;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.GizmoRegister;
import com.talosvfx.talos.editor.utils.CameraController;
import com.talosvfx.talos.editor.widgets.ui.gizmos.Gizmos;

public abstract class ViewportWidget extends Table {

    protected OrthographicCamera camera;

    protected Matrix4 emptyTransform = new Matrix4();
    private Matrix4 prevTransform  = new Matrix4();
    private Matrix4 prevProjection = new Matrix4();

    public CameraController cameraController;

    protected Color bgColor = new Color(Color.BLACK);

    protected float maxZoom = 0.01f;
    protected float minZoom = 200f;

    protected ShapeRenderer shapeRenderer;
    private float gridSize;
    private float worldWidth = 1f;

    private Vector3 tmp = new Vector3();
    private Vector2 vec2 = new Vector2();

    protected Gizmos gizmos = new Gizmos();


    public ViewportWidget() {
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        camera.viewportWidth = 10;

        setTouchable(Touchable.enabled);

        cameraController = new CameraController(camera);
        cameraController.setInvert(true);

        addPanListener();

    }

    public void selectGizmos (Array<GameObject> gameObjects) {
        for (Gizmo gizmo : this.gizmos.gizmoList) {
            gizmo.setSelected(false);
        }

        if (gameObjects.size == 1) {
            Array<Gizmo> gizmos = this.gizmos.gizmoMap.get(gameObjects.get(0));
            if (gizmos != null) {
                for (Gizmo gizmo : gizmos) {
                    gizmo.setSelected(true);
                }
            }
        } else {
            for (GameObject gameObject : gameObjects) {
                Array<Gizmo> gizmos = this.gizmos.gizmoMap.get(gameObject);
                if (gizmos != null) {
                    for (Gizmo gizmo : gizmos) {
                        if (gizmo.isMultiSelect()) {
                            gizmo.setSelected(true);
                        }
                    }
                }
            }
        }
    }


    private void removeGizmos () {
        //todo clear on change, not on start
        for (Gizmo gizmo : gizmos.gizmoList) {
            gizmo.remove();
        }
        gizmos.gizmoList.clear();
        gizmos.gizmoMap.clear();
    }

    public void removeGizmos (GameObject gameObject) {
        Array<Gizmo> list = gizmos.gizmoMap.get(gameObject);
        if (list == null) {
            System.out.println("No gimzmo for " + gameObject);
            return;
        } else {
            System.out.println("removing gizmos for " + gameObject);
        }
        for (Gizmo gizmo : list) {
            gizmo.notifyRemove();
            gizmo.remove();
        }
        gizmos.gizmoList.removeAll(list, true);
        gizmos.gizmoMap.remove(gameObject);
    }

    public void initGizmos (GameObject gameObject, ViewportWidget parent) {
        makeGizmosFor(gameObject, parent);
        Array<GameObject> childObjects = gameObject.getGameObjects();
        if (childObjects != null) {
            for (GameObject childObject : childObjects) {
                makeGizmosFor(childObject, parent);
                initGizmos(childObject, parent);
            }
        }
    }

    public void initGizmos (GameObjectContainer gameObjectContainer, ViewportWidget parent) {
        Array<GameObject> childObjects = gameObjectContainer.getGameObjects();
        if (childObjects != null) {
            for (GameObject childObject : childObjects) {
                initGizmos(childObject, parent);
            }
        }
    }

    public void makeGizmosFor (GameObject gameObject, ViewportWidget parent) {
        if (parent.gizmos.gizmoMap.containsKey(gameObject))
            return;

        Iterable<AComponent> components = gameObject.getComponents();
        for (AComponent component : components) {
            Array<Gizmo> gizmos = GizmoRegister.makeGizmosFor(component);

            for (Gizmo gizmo : gizmos) {
                if (gizmo != null) {
                    gizmo.setGameObject(gameObject);

                    Array<Gizmo> list = parent.gizmos.gizmoMap.get(gameObject);
                    if (list == null)
                        list = new Array<>();

                    parent.gizmos.gizmoMap.put(gameObject, list);

                    if (gizmo != null) {
                        parent.gizmos.gizmoList.add(gizmo);
                        list.add(gizmo);
                    }
                }
            }
        }
    }

    protected Gizmo hitGizmo (float x, float y) {
        for (Gizmo gizmo : gizmos.gizmoList) {
            if (gizmo.hit(x, y))
                return gizmo;
        }

        return null;
    }

    protected void addPanListener() {
        addListener(new InputListener() {
            @Override
            public boolean scrolled (InputEvent event, float x, float y, float amountX, float amountY) {
                float currWidth = camera.viewportWidth * camera.zoom;
                float nextWidth = currWidth * (1f + amountY * 0.1f);
                float nextZoom = nextWidth/camera.viewportWidth;
                camera.zoom = nextZoom;

                camera.zoom = MathUtils.clamp(camera.zoom, minZoom, maxZoom);
                camera.update();

                return true;
            }

            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                cameraController.touchDown((int)x, (int)y, pointer, button);
                return !event.isHandled();
            }

            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                cameraController.touchUp((int)x, (int)y, pointer, button);
            }

            @Override
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                cameraController.touchDragged((int)x, (int)y, pointer);
            }

            @Override
            public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                TalosMain.Instance().UIStage().getStage().setScrollFocus(ViewportWidget.this);
            }

            @Override
            public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                if (pointer != -1) return; //Only care about exit/enter from mouse move
                TalosMain.Instance().UIStage().getStage().setScrollFocus(null);
            }
        });
    }

    Vector2 temp = new Vector2();

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();

        localToScreenCoordinates(temp.set(0, 0));
        int x = (int)temp.x;
        int y = (int)temp.y;

        localToScreenCoordinates(temp.set(getWidth(), getHeight()));

        int x2 = (int)temp.x;
        int y2 = (int)temp.y;

        int ssWidth = x2 - x;
        int ssHeight = y - y2;

        HdpiUtils.glViewport(x, Gdx.graphics.getHeight() - y, ssWidth, ssHeight);

        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float aspect = getWidth()/getHeight();

        camera.viewportHeight = camera.viewportWidth / aspect;

        camera.update();

        prevTransform.set(batch.getTransformMatrix());
        prevProjection.set(batch.getProjectionMatrix());
        batch.setProjectionMatrix(camera.combined);
        batch.setTransformMatrix(emptyTransform);

        batch.begin();
        drawContent(batch, parentAlpha);

        for (int i = 0; i < this.gizmos.gizmoList.size; i++) {
            Gizmo gizmo = this.gizmos.gizmoList.get(i);
            gizmo.setWoldWidth(getWorldWidth() * camera.zoom);
            gizmo.draw(batch, parentAlpha);
        }

        batch.end();

        HdpiUtils.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.setProjectionMatrix(prevProjection);
        batch.setTransformMatrix(prevTransform);
        batch.begin();

        super.draw(batch, parentAlpha);
    }

    protected void drawGroup(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    @Override
    public void act (float delta) {
        super.act(delta);

        for (int i = 0; i < this.gizmos.gizmoList.size; i++) {
            Gizmo gizmo = this.gizmos.gizmoList.get(i);
            gizmo.act(delta);
        }
    }

    public abstract void drawContent(Batch batch, float parentAlpha);

    public OrthographicCamera getCamera() {
        return camera;
    }
    public float getCameraPosX() {
        return camera.position.x;
    }

    public float getCameraPosY() {
        return camera.position.y;
    }

    public float getCameraZoom() {
        return camera.zoom;
    }

    public void setCameraPos(float x, float y) {
        camera.position.set(x, y, 0);
    }

    public void setCameraZoom(float zoom) {
        camera.zoom = zoom;
    }

    public void setViewportWidth(float width) {
        camera.viewportWidth = width;
        camera.update();
    }

    protected void setWorldSize(float worldWidth) {
        this.worldWidth = worldWidth;
        updateNumbers();
    }

    private void updateNumbers() {
        camera.zoom = worldWidth/camera.viewportWidth;
        gridSize = worldWidth/40f;
        float minWidth = gridSize * 4f;
        float maxWidth = worldWidth * 10f;
        minZoom = minWidth/camera.viewportWidth;
        maxZoom = maxWidth/camera.viewportWidth;
        camera.update();
    }

    protected void resetCamera() {
        camera.position.set(0, 0, 0);
        camera.zoom = worldWidth/camera.viewportWidth;
    }

    protected void drawGrid(Batch batch, float parentAlpha) {
        Gdx.gl.glLineWidth(1f);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        Color gridColor = new Color(Color.GRAY);

        int minCount = 3;
        int maxCount = 100;

        float width = camera.viewportWidth * camera.zoom;
        float height = camera.viewportHeight * camera.zoom;

        float x = camera.position.x;
        float y = camera.position.y;

        int countX = MathUtils.ceil(width/gridSize);
        int countY = MathUtils.ceil(height/gridSize);

        float falloff = ((float)(MathUtils.clamp(countX, minCount, maxCount)-minCount))/(maxCount-minCount);
        float brightAlpha = (1f-falloff*0.95f) * 0.3f * parentAlpha;
        float dimAlpha = gridColor.a * 0.1f * parentAlpha;

        // camera offsets
        x =  x - x % (gridSize*8f);
        y =  y - y % (gridSize*8f);

        float thickness = pixelToWorld(1.2f);

        for(int i = -countX/2-8; i <= countX/2+8; i++) {
            if(i % 4 == 0) gridColor.a = brightAlpha;
            else gridColor.a = dimAlpha;
            shapeRenderer.setColor(gridColor);
            shapeRenderer.rectLine(i * gridSize + x , -height/2f + y - gridSize*8f, i * gridSize + x, height/2f + y + gridSize*8f, thickness);
        }
        for(int i = -countY/2-8; i <= countY/2+8; i++) {
            if(i % 4 == 0) gridColor.a = brightAlpha;
            else gridColor.a = dimAlpha;
            shapeRenderer.setColor(gridColor);
            shapeRenderer.rectLine(-width/2f + x - gridSize * 8f, i * gridSize + y, width/2f + x + gridSize*8f, i * gridSize + y, thickness);
        }

        shapeRenderer.end();
    }



    private void getViewportBounds (Rectangle out) {
        localToScreenCoordinates(temp.set(0, 0));
        int x = (int)temp.x;
        int y = (int)temp.y;

        localToScreenCoordinates(temp.set(getWidth(), getHeight()));

        int x2 = (int)temp.x;
        int y2 = (int)temp.y;

        int ssWidth = x2 - x;
        int ssHeight = y - y2;

        y = Gdx.graphics.getHeight() - y;

        out.set(x, y, ssWidth, ssHeight);
    }

    protected Vector2 getLocalFromWorld (float x, float y) {
        getViewportBounds(Rectangle.tmp);
        camera.project(tmp.set(x, y, 0), Rectangle.tmp.x, Rectangle.tmp.y, Rectangle.tmp.width, Rectangle.tmp.height);
        Vector2 vector2 = screenToLocalCoordinates(new Vector2(tmp.x, (Rectangle.tmp.height - tmp.y) + Rectangle.tmp.y + 50)); // 50 is top bar height :(((((
        vec2.set(vector2);

        return vec2;
    }

    public Vector2 getWorldFromLocal (float x, float y) {
        Vector2 vector2 = localToScreenCoordinates(new Vector2(x, y));

        getViewportBounds(Rectangle.tmp);

        camera.unproject(tmp.set(vector2.x, vector2.y, 0), Rectangle.tmp.x, Rectangle.tmp.y, Rectangle.tmp.width, Rectangle.tmp.height);

        vec2.set(tmp.x, tmp.y);

        return vec2;
    }

    protected Vector3 getWorldFromLocal (Vector3 vec) {
        Vector2 vector2 = localToScreenCoordinates(new Vector2(vec.x, vec.y));

        getViewportBounds(Rectangle.tmp);

        camera.unproject(vec.set(vector2.x, vector2.y, 0), Rectangle.tmp.x, Rectangle.tmp.y, Rectangle.tmp.width, Rectangle.tmp.height);
        return vec;
    }

    public Vector3 getTouchToLocal (float x, float y) {
        Vector3 vec = new Vector3(x, y, 0);

        getViewportBounds(Rectangle.tmp);

        camera.unproject(vec.set(vec.x, vec.y, 0), Rectangle.tmp.x, Rectangle.tmp.y, Rectangle.tmp.width, Rectangle.tmp.height);
        return vec;
    }

    protected float pixelToWorld(float pixelSize) {
        tmp.set(0, 0, 0);
        camera.unproject(tmp);
        float baseline = tmp.x;

        tmp.set(pixelSize, 0, 0);
        camera.unproject(tmp);
        float pos = tmp.x;

        return Math.abs(pos - baseline) * (getStage().getWidth()/getWidth()); //TODO: I am sure there is a better way to do this
    }

    public float getWorldWidth() {
        return worldWidth;
    }
}
