package com.talosvfx.talos.editor.project2;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

public class SharedStage extends Stage {

    public SharedStage () {
        super();
    }
    public SharedStage (Viewport viewport) {
       super(viewport);
    }

    public SharedStage (Viewport viewport, Batch batch) {
       super(viewport, batch);
    }

    @Override
    public boolean setKeyboardFocus(Actor actor) {
        boolean focus = super.setKeyboardFocus(actor);
        if (focus) {
            SharedResources.inputHandling.keyboardFocus = actor;
        }
        return focus;
    }
}
