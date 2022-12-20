package com.talosvfx.talos.editor.addons.scene.apps.routines;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.RoutineExposedVariableNodeWidget;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineConfigMap;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineInstance;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.nodes.NodeBoard;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.vfxui.GenericStageWrappedViewportWidget;

public class RoutineEditorApp extends AppManager.BaseApp<RoutineData> {

    private RoutineConfigMap routineConfigMap;
    public RoutineStage routineStage;
    public VariableCreationWindow variableCreationWindow;

    public ScenePreviewStage scenePreviewStage;

    public SplitPane splitPane;

    private Table content;

    public RoutineEditorApp() {
        routineConfigMap = new RoutineConfigMap();
        FileHandle handle = Gdx.files.internal("addons/scene/tween-nodes.xml");
        routineConfigMap.loadFrom(handle);
        //variableCreationWindow = new VariableCreationWindow();
        //variableCreationWindow.reloadWidgets(routineStage);

        initContent();

        DummyLayoutApp app = new DummyLayoutApp(SharedResources.skin, getAppName()) {
            @Override
            public Actor getMainContent() {
                return content;
            }
        };

        this.gridAppReference = app;

    }

    public void initContent() {

        content = new Table();
        routineStage = new RoutineStage(this, SharedResources.skin);
        routineStage.init();
        routineStage.routineConfigMap = routineConfigMap;
        scenePreviewStage = new ScenePreviewStage();

        GenericStageWrappedViewportWidget routineStageWrapper = new GenericStageWrappedViewportWidget(routineStage.getContainer());
        try {

            routineStage.loadFrom(gameAsset);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Table table = new Table();
        table.add(routineStageWrapper).grow();
        //table.addActor(variableCreationWindow);
        splitPane = new SplitPane(scenePreviewStage, table,  false, SharedResources.skin);
        splitPane.setSplitAmount(0.2f);
        content.add(splitPane).grow();
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

        routineStage.reloadRoutineInstancesFromMemory();
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
        routineStage.reloadRoutineInstancesFromMemory();
    }

    public void changeTypeFor (int index, String newType) {
        RoutineInstance routineInstance = routineStage.routineInstance;
        routineInstance.changeExposedVariableType(index, newType);

        variableCreationWindow.reloadWidgets(routineStage);
        routineStage.reloadRoutineInstancesFromMemory();
    }

    public void createNewVariable () {
        RoutineInstance routineInstance = routineStage.routineInstance;
        routineInstance.createNewPropertyWrapper();
        variableCreationWindow.reloadWidgets(routineStage);
        routineStage.reloadRoutineInstancesFromMemory();
    }

    @Override
    public String getAppName() {
        if(gameAsset == null) {
            return "null"; // lol wtf
        }
        return gameAsset.nameIdentifier;
    }

    @Override
    public void onRemove() {
        // remove listeners and stuff somehow
    }
}
