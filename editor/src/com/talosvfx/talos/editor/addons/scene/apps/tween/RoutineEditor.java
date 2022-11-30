package com.talosvfx.talos.editor.addons.scene.apps.tween;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.apps.AEditorApp;
import com.talosvfx.talos.editor.addons.scene.apps.tween.nodes.RoutineExposedVariableNodeWidget;
import com.talosvfx.talos.editor.addons.scene.apps.tween.nodes.RoutineNodeWidget;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.RoutineConfigMap;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.RoutineInstance;
import com.talosvfx.talos.editor.addons.scene.utils.scriptProperties.*;
import com.talosvfx.talos.editor.nodes.NodeBoard;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.nodes.widgets.TextValueWidget;

public class RoutineEditor extends AEditorApp<FileHandle> {

    private final RoutineConfigMap routineConfigMap;
    private String title;
    public RoutineStage routineStage;
    public AnimationTimeline animationTimeline;
    public VariableCreationWindow variableCreationWindow;

    public FileHandle targetFileHandle;

    public ScenePreviewStage scenePreviewStage;

    public SplitPane splitPane;

    public RoutineEditor(FileHandle twFileHandle) {
        super(twFileHandle);
        identifier = twFileHandle.path();
        title = twFileHandle.name();
        targetFileHandle = twFileHandle;

        routineConfigMap = new RoutineConfigMap();
        FileHandle handle = Gdx.files.internal("addons/scene/tween-nodes.xml");
        routineConfigMap.loadFrom(handle);
        variableCreationWindow = new VariableCreationWindow();

        initContent();

        addAppListener(new AppListener() {
            @Override
            public void closeRequested () {
                SceneEditorAddon.get().routineEditor = null;
                TalosMain.Instance().getInputMultiplexer().removeProcessor(routineStage.getStage());
            }
        });

        variableCreationWindow.reloadWidgets(routineStage);
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
            e.printStackTrace();
        }

        scenePreviewStage = new ScenePreviewStage();
        Table table = new Table();
        table.add(routineStage.getContainer()).grow();
        table.addActor(variableCreationWindow);
        splitPane = new SplitPane(scenePreviewStage, table,  false, TalosMain.Instance().getSkin());
        splitPane.setSplitAmount(0.2f);

        content.add(splitPane).grow();

        TalosMain.Instance().getInputMultiplexer().addProcessor(routineStage.getStage());

    }

    @Override
    public String getTitle() {
        return title;
    }

    public void deleteParamTemplateWithIndex (int index) {
        RoutineInstance routineInstance = routineStage.routineInstance;
        routineInstance.removeExposedVariablesWithIndex(index);
        variableCreationWindow.reloadWidgets(routineStage);

        NodeBoard nodeBoard = routineStage.getNodeBoard();

        for (NodeWidget node : nodeBoard.nodes) {
            if (node instanceof RoutineExposedVariableNodeWidget) {
                RoutineExposedVariableNodeWidget widget = ((RoutineExposedVariableNodeWidget) node);
                if (widget.index == index) {
                    widget.update(null);
                }
            }
        }

        routineStage.routineUpdated();
    }

    public void changeKeyFor (int index, String value) {
        RoutineInstance routineInstance = routineStage.routineInstance;
        routineInstance.changeExposedVariableKey(index, value);
        NodeBoard nodeBoard = routineStage.getNodeBoard();

        for (NodeWidget node : nodeBoard.nodes) {
            if (node instanceof RoutineExposedVariableNodeWidget) {
                RoutineExposedVariableNodeWidget widget = ((RoutineExposedVariableNodeWidget) node);
                if (widget.index == index) {
                    widget.update(routineInstance.getPropertyWrapperWithIndex(index));
                }
            }
        }
        routineStage.routineUpdated();
    }

    public void changeTypeFor (int index, String newType) {
        RoutineInstance routineInstance = routineStage.routineInstance;
        routineInstance.changeExposedVariableType(index, newType);

        variableCreationWindow.reloadWidgets(routineStage);
        routineStage.routineUpdated();
    }

    public void createNewVariable () {
        RoutineInstance routineInstance = routineStage.routineInstance;
        routineInstance.createNewPropertyWrapper();
        variableCreationWindow.reloadWidgets(routineStage);
        routineStage.routineUpdated();
    }
}
