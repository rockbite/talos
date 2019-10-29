package com.rockbite.tools.talos.editor.addons.bvb;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rockbite.tools.talos.editor.widgets.ui.ViewportWidget;

public class BvBWorkspace extends ViewportWidget {

    ShapeRenderer shapeRenderer;

    BvBWorkspace() {
        shapeRenderer = new ShapeRenderer();
        setWorldWidth(10f);
        setCameraPos(0, 0);
    }

    @Override
    public void drawContent(Batch batch, float parentAlpha) {
        batch.end();

        Gdx.gl.glLineWidth(1f);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);

        shapeRenderer.circle(0, 0, 1f, 20);
        shapeRenderer.end();

        batch.begin();
    }
}
