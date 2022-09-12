package com.talosvfx.talos.editor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.talosvfx.talos.editor.utils.grid.GridRenderer;
import com.talosvfx.talos.editor.utils.grid.property_providers.DynamicGridPropertyProvider;

public class GridRendererWrapper extends Actor {

    private final Stage stage;
    private final OrthographicCamera camera;

    private ShapeRenderer shapeRenderer;

    GridRenderer gridRenderer;

    DynamicGridPropertyProvider gridPropertyProvider;

    private Vector3 tmp = new Vector3();

    public GridRendererWrapper (Stage stage) {
        this.stage = stage;
        camera = (OrthographicCamera)this.stage.getViewport().getCamera();
        shapeRenderer = new ShapeRenderer();
        gridPropertyProvider = new DynamicGridPropertyProvider();
        gridPropertyProvider.setLineThickness(pixelToWorld(1.2f));
        gridPropertyProvider.distanceThatLinesShouldBe = pixelToWorld(150f);
        gridPropertyProvider.getBackgroundColor().set(Color.valueOf("#272727"));
        gridPropertyProvider.hideZero();
        gridRenderer = new GridRenderer(gridPropertyProvider, null);
    }

    @Override
    public void act (float delta) {
        super.act(delta);
        gridPropertyProvider.distanceThatLinesShouldBe = pixelToWorld(150f);
        gridPropertyProvider.setLineThickness(pixelToWorld(1.2f));
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        gridPropertyProvider.update(camera, parentAlpha);
        batch.end();
        gridRenderer.drawGrid(batch, shapeRenderer);
        batch.begin();
    }

    protected float pixelToWorld (float pixelSize) {
        tmp.set(0, 0, 0);
        camera.unproject(tmp);
        float baseline = tmp.x;

        tmp.set(pixelSize, 0, 0);
        camera.unproject(tmp);
        float pos = tmp.x;

        return Math.abs(pos - baseline);
    }
}
