package com.rockbite.tools.talos.editor.widgets.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Group;

public abstract class ViewportWidget extends Group {

    protected OrthographicCamera camera;

    protected Matrix4 emptyTransform = new Matrix4();
    private Matrix4 prevTransform  = new Matrix4();
    private Matrix4 prevProjection = new Matrix4();

    public ViewportWidget() {
        camera = new OrthographicCamera();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();

        HdpiUtils.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        camera.viewportWidth = Gdx.graphics.getWidth();
        camera.viewportHeight = Gdx.graphics.getHeight();
        camera.zoom = 1f;
        camera.update();

        prevTransform.set(batch.getTransformMatrix());
        prevProjection.set(batch.getProjectionMatrix());
        batch.setProjectionMatrix(camera.combined);
        batch.setTransformMatrix(emptyTransform);

        batch.begin();
        drawContent(batch, parentAlpha);
        batch.end();

        HdpiUtils.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.setProjectionMatrix(prevProjection);
        batch.setTransformMatrix(prevTransform);
        batch.begin();
    }

    protected void drawGroup(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    public abstract void drawContent(Batch batch, float parentAlpha);

}
