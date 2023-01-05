package com.talosvfx.talos.editor;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.editor.notifications.ContextRequiredEvent;
import lombok.Getter;

public abstract class WorkplaceStage {


    @Getter
    private Table rootActor;

    public WorkplaceStage() {
        rootActor = new Table();
        rootActor.setFillParent(true);
        rootActor.setTouchable(Touchable.enabled);
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
