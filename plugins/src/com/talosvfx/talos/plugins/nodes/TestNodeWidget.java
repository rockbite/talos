package com.talosvfx.talos.plugins.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.nodes.NodeWidget;

public class TestNodeWidget extends NodeWidget {

    @Override
    public void constructNode (XmlReader.Element module) {
        System.out.println("Constructed test custom node widget");

        setSize(500,500);
        super.constructNode(module);
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        batch.end();

        ShapeRenderer shapeRenderer = new ShapeRenderer();
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
        shapeRenderer.end();

        shapeRenderer.dispose();

        batch.begin();

    }
}
