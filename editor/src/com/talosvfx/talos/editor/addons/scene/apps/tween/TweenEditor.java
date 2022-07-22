package com.talosvfx.talos.editor.addons.scene.apps.tween;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.apps.AEditorApp;

public class TweenEditor extends AEditorApp<FileHandle> {

    private String title;
    public TweenStage tweenStage;
    public AnimationTimeline animationTimeline;

    public TweenEditor(FileHandle twFileHandle) {
        super(twFileHandle);
        identifier = twFileHandle.path();
        title = twFileHandle.name();
        initContent();
    }

    @Override
    public void initContent() {
        content = new Table();

        Skin skin = TalosMain.Instance().UIStage().getSkin();

        animationTimeline = new AnimationTimeline(skin);

        tweenStage = new TweenStage(this, skin);
        tweenStage.init();


        SplitPane splitPane = new SplitPane(animationTimeline, tweenStage.getContainer(), false, skin);
        content.add(splitPane).grow();

        TalosMain.Instance().getInputMultiplexer().addProcessor(tweenStage.getStage());

    }

    @Override
    public String getTitle() {
        return title;
    }
}
