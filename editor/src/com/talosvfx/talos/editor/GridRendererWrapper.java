package com.talosvfx.talos.editor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.talosvfx.talos.editor.utils.grid.GridPropertyProvider;
import com.talosvfx.talos.editor.utils.grid.GridRenderer;
import com.talosvfx.talos.editor.utils.grid.property_providers.ParticleNodeGridPropertyProvider;

public class GridRendererWrapper extends Actor {

    private final Stage stage;
    private final OrthographicCamera camera;

    private ShapeRenderer shapeRenderer;

    GridRenderer gridRenderer;

    ParticleNodeGridPropertyProvider gridPropertyProvider;

    public GridRendererWrapper (Stage stage) {
        this.stage = stage;
        camera = (OrthographicCamera)this.stage.getViewport().getCamera();
        shapeRenderer = new ShapeRenderer();
        gridPropertyProvider = new ParticleNodeGridPropertyProvider();
        gridPropertyProvider.getBackgroundColor().set(Color.valueOf("#272727"));
        gridRenderer = new GridRenderer(gridPropertyProvider, null);
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        gridPropertyProvider.update(camera, parentAlpha);
        batch.end();
        gridRenderer.drawGrid(batch, shapeRenderer);
        batch.begin();
    }
}
