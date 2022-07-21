package com.talosvfx.talos.editor.addons.scene.apps.tween;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.apps.AEditorApp;

public class TweenEditor extends AEditorApp<FileHandle> {

    private String title;

    public TweenEditor(FileHandle twFileHandle) {
        super(twFileHandle);
        identifier = twFileHandle.path();
        title = twFileHandle.name();
        initContent();
    }

    @Override
    public void initContent() {
        content = new Table();

        TweenStage tweenStage = new TweenStage(TalosMain.Instance().UIStage().getSkin());
        tweenStage.init();

        content.add(tweenStage.getContainer()).grow();

        TalosMain.Instance().getInputMultiplexer().addProcessor(tweenStage.getStage());

    }

    @Override
    public String getTitle() {
        return title;
    }
}
