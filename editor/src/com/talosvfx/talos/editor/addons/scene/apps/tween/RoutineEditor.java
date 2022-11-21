package com.talosvfx.talos.editor.addons.scene.apps.tween;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.apps.AEditorApp;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.RoutineConfigMap;

public class RoutineEditor extends AEditorApp<FileHandle> {

    private final RoutineConfigMap routineConfigMap;
    private String title;
    public RoutineStage routineStage;
    public AnimationTimeline animationTimeline;

    public FileHandle targetFileHandle;

    public ScenePreviewStage scenePreviewStage;

    public RoutineEditor(FileHandle twFileHandle) {
        super(twFileHandle);
        identifier = twFileHandle.path();
        title = twFileHandle.name();
        targetFileHandle = twFileHandle;

        routineConfigMap = new RoutineConfigMap();
        FileHandle handle = Gdx.files.internal("addons/scene/tween-nodes.xml");
        routineConfigMap.loadFrom(handle);

        initContent();
    }

    @Override
    public void initContent() {
        content = new Table();

        Skin skin = TalosMain.Instance().UIStage().getSkin();

        /*
        animationTimeline = new AnimationTimeline(skin);
        SplitPane splitPane = new SplitPane(animationTimeline, , false, skin);
        */
        routineStage = new RoutineStage(this, skin);
        routineStage.init();
        routineStage.routineConfigMap = routineConfigMap;

        try {
            routineStage.loadFrom(targetFileHandle);
        } catch (Exception e) {
            System.out.println(e);
        }

        scenePreviewStage = new ScenePreviewStage();
        SplitPane splitPane = new SplitPane(scenePreviewStage, routineStage.getContainer(),  false, TalosMain.Instance().getSkin());
        splitPane.setSplitAmount(0.2f);

        content.add(splitPane).grow();

        TalosMain.Instance().getInputMultiplexer().addProcessor(routineStage.getStage());

    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void onHide () {
        super.onHide();
        SceneEditorAddon.get().routineEditor = null;
    }

}
