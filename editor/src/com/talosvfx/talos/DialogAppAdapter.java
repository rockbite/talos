package com.talosvfx.talos;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.rockbite.bongo.engine.render.PolygonSpriteBatchMultiTextureMULTIBIND;
import com.talosvfx.talos.editor.dialogs.IWindowDialog;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.SharedStage;
import lombok.Setter;

public class DialogAppAdapter extends ApplicationAdapter {

    private final IWindowDialog dialog;
    @Setter
    private Runnable disposeRunnable;
    private Stage stage;

    public DialogAppAdapter(IWindowDialog dialog) {
        this.dialog = dialog;
    }

    @Override
    public void create() {
        stage = new SharedStage(new ScreenViewport(), new PolygonSpriteBatchMultiTextureMULTIBIND());

        Table content = dialog.getContent();
        stage.addActor(content);
        content.setFillParent(true);

        //We have our own input, so this is fine
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1f, true);
        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();

        if(disposeRunnable != null) {
            disposeRunnable.run();
        }
    }
}
