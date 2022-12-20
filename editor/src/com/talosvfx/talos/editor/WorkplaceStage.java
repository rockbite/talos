package com.talosvfx.talos.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.rockbite.bongo.engine.render.PolygonSpriteBatchMultiTextureMULTIBIND;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.utils.CameraController;
import lombok.Getter;

public abstract class WorkplaceStage {


    @Getter
    private Table rootActor;

    public WorkplaceStage() {
        rootActor = new Table();
        rootActor.setFillParent(true);
        rootActor.setTouchable(Touchable.enabled);
        rootActor.debugAll();
    }


    public abstract void init();

    public void resize (int width, int height) {
    }


    protected void initListeners() {
    }

    public void act() {

    }

    public  abstract void fileDrop(String[] paths, float x, float y);
}
