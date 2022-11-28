package com.talosvfx.talos.editor.addons.scene.apps.tween;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.apps.AEditorApp;
import com.talosvfx.talos.editor.project2.SharedResources;

public class TweenEditor extends AEditorApp<FileHandle> {

    private String title;
    public TweenStage tweenStage;
    public AnimationTimeline animationTimeline;

    public FileHandle targetFileHandle;

    public ScenePreviewStage scenePreviewStage;

    public TweenEditor(FileHandle twFileHandle) {
        super(twFileHandle);
        identifier = twFileHandle.path();
        title = twFileHandle.name();
        targetFileHandle = twFileHandle;
        initContent();
    }

    @Override
    public void initContent() {
        content = new Table();

        Skin skin = SharedResources.skin;

        /*
        animationTimeline = new AnimationTimeline(skin);
        SplitPane splitPane = new SplitPane(animationTimeline, , false, skin);
        */
        tweenStage = new TweenStage(this, skin);
        tweenStage.init();

        try {
            Json json = new Json();
            JsonReader jsonReader = new JsonReader();
            tweenStage.read(json, jsonReader.parse(targetFileHandle));
        } catch (Exception e) {
            System.out.println(e);
        }

        scenePreviewStage = new ScenePreviewStage();
        SplitPane splitPane = new SplitPane(scenePreviewStage, tweenStage.getContainer(),  false, SharedResources.skin);
        splitPane.setSplitAmount(0.2f);

        content.add(splitPane).grow();

//        TalosMain.Instance().getInputMultiplexer().addProcessor(tweenStage.getStage());

    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void onHide () {
        super.onHide();
    }

}
