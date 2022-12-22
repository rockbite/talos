package com.talosvfx.talos.editor.addons.scene.apps.routines;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.RoutineExposedVariableNodeWidget;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineConfigMap;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineInstance;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers.PropertyType;
import com.talosvfx.talos.editor.data.RoutineStageData;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.nodes.NodeBoard;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.vfxui.GenericStageWrappedViewportWidget;

public class RoutineEditorApp extends AppManager.BaseApp<RoutineStageData> {

    private RoutineConfigMap routineConfigMap;
    public RoutineStage routineStage;
    public VariableCreationWindow variableCreationWindow;

    public GenericStageWrappedViewportWidget routineStageWrapper;

//    public ScenePreviewStage scenePreviewStage;

    public RoutineEditorApp() {
        routineConfigMap = new RoutineConfigMap();
        FileHandle handle = Gdx.files.internal("addons/scene/tween-nodes.xml");
        routineConfigMap.loadFrom(handle);

        routineStage = new RoutineStage(this, SharedResources.skin);
        routineStageWrapper = new GenericStageWrappedViewportWidget(routineStage.getRootActor()) {
            @Override
            protected boolean canMoveAround() {
                return true;
            }
        };
        routineStageWrapper.getDropdownForWorld().setVisible(false);

        final Table content = new Table();
        Table separator = new Table();
        separator.setBackground(SharedResources.skin.newDrawable("white", Color.valueOf("#505050ff")));
        content.add(separator).growX().height(3).row();
        content.add(routineStageWrapper).grow();

        routineStage.init();
        routineStage.routineConfigMap = routineConfigMap;
//        scenePreviewStage = new ScenePreviewStage();

        variableCreationWindow = new VariableCreationWindow(routineStage);


        routineStageWrapper.left().top();
        routineStageWrapper.add(variableCreationWindow).pad(10).width(240);

        routineStage.sendInStage(routineStageWrapper.getStage());

        DummyLayoutApp app = new DummyLayoutApp(SharedResources.skin, getAppName()) {
            @Override
            public Actor getMainContent() {
                return content;
            }

            @Override
            public void onInputProcessorAdded () {
                super.onInputProcessorAdded();
                routineStageWrapper.restoreListeners();
                SharedResources.stage.setScrollFocus(routineStageWrapper);
                SharedResources.inputHandling.addPriorityInputProcessor(routineStageWrapper.getStage());
                SharedResources.inputHandling.setGDXMultiPlexer();
            }

            @Override
            public void onInputProcessorRemoved () {
                super.onInputProcessorRemoved();
                routineStageWrapper.disableListeners();
                SharedResources.inputHandling.removePriorityInputProcessor(routineStageWrapper.getStage());
                SharedResources.inputHandling.setGDXMultiPlexer();

                Stage stage = routineStageWrapper.getStage();
            }
        };

        this.gridAppReference = app;
    }

    @Override
    public void updateForGameAsset (GameAsset<RoutineStageData> gameAsset) {
        super.updateForGameAsset(gameAsset);

        routineStage.loadFrom(gameAsset);
        variableCreationWindow.reloadWidgets();

    }

//    public void deleteParamTemplateWithIndex (int index) {
//        RoutineInstance routineInstance = routineStage.routineInstance;
//        routineInstance.removeExposedVariablesWithIndex(index);
//        variableCreationWindow.reloadWidgets();
//
//        NodeBoard nodeBoard = routineStage.getNodeBoard();
//
//        for (NodeWidget node : routineStage..nodes) {
//            if (node instanceof RoutineExposedVariableNodeWidget) {
//                RoutineExposedVariableNodeWidget widget = ((RoutineExposedVariableNodeWidget) node);
//                if (widget.index == index) {
//                    widget.update(null);
//                }
//            }
//        }
//
//        routineStage.routineUpdated();
//    }
//
//    public void changeKeyFor (int index, String value) {
//        RoutineInstance routineInstance = routineStage.routineInstance;
//        routineInstance.changeExposedVariableKey(index, value);
//        NodeBoard nodeBoard = routineStage.getNodeBoard();
//
//        for (NodeWidget node : nodeBoard.nodes) {
//            if (node instanceof RoutineExposedVariableNodeWidget) {
//                RoutineExposedVariableNodeWidget widget = ((RoutineExposedVariableNodeWidget) node);
//                if (widget.index == index) {
//                    widget.update(routineInstance.getPropertyWrapperWithIndex(index));
//                }
//            }
//        }
//        routineStage.routineUpdated();
//    }
//
//    public void changeTypeFor (int index, PropertyType newType) {
//        RoutineInstance routineInstance = routineStage.routineInstance;
//        routineInstance.changeExposedVariableType(index, newType);
//
//        variableCreationWindow.reloadWidgets();
//        routineStage.routineUpdated();
//    }
//
//    public void createNewVariable (PropertyType type) {
//        RoutineInstance routineInstance = routineStage.routineInstance;
//        routineInstance.createNewPropertyWrapper(type);
//        variableCreationWindow.reloadWidgets();
//        routineStage.routineUpdated();
//    }

    @Override
    public String getAppName() {
        if(gameAsset == null) {
            return "Routine"; // lol wtf
        }
        return gameAsset.nameIdentifier;
    }

    @Override
    public void onRemove() {
        // remove listeners and stuff somehow
    }
}
