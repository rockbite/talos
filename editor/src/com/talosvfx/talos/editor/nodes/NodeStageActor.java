package com.talosvfx.talos.editor.nodes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.tabbedpane.Tab;

public class NodeStageActor extends Table {

    DynamicNodeStage nodeStage;
    private Matrix4 transform;

    NodeStageActor(DynamicNodeStage nodeStage) {
        this.nodeStage = nodeStage;
        transform = new Matrix4();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Matrix4 transformMatrix = batch.getTransformMatrix();
        batch.flush();
        batch.setTransformMatrix(transform);

        clipBegin();
        nodeStage.act();
        nodeStage.getStage().act();
        nodeStage.getStage().draw();
        clipEnd();

        batch.setTransformMatrix(transformMatrix);
    }
}
