package com.talosvfx.talos.plugins.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.nodes.PluginNodeWidget;
import com.talosvfx.talos.plugins.InternalPluginProvider;

public class TestNodeWidget extends PluginNodeWidget<InternalPluginProvider> {

    public TestNodeWidget (Skin skin) {
        super(skin);
    }

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
