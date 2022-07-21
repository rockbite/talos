package com.talosvfx.talos.editor.nodes;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class NodeStageActor extends Actor {

    DynamicNodeStage nodeStage;

    NodeStageActor(DynamicNodeStage nodeStage) {
        this.nodeStage = nodeStage;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        clipBegin();
        nodeStage.act();
        nodeStage.getStage().act();
        nodeStage.getStage().draw();
        clipEnd();
    }
}
