package com.talosvfx.talos.editor.addons.scene.apps.routines;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.data.RoutineStageData;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.vfxui.GenericStageWrappedViewportWidget;

public class RoutineEditorApp extends AppManager.BaseApp<RoutineStageData> implements GameAsset.GameAssetUpdateListener {
    public RoutineStage routineStage;
    public VariableCreationWindow variableCreationWindow;

    public GenericStageWrappedViewportWidget routineStageWrapper;

//    public ScenePreviewStage scenePreviewStage;

    public RoutineEditorApp() {
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
        if (this.gameAsset != null) {
            this.gameAsset.listeners.removeValue(this, true);
        }

        if (!gameAsset.listeners.contains(this, true)) {
            gameAsset.listeners.add(this);
        }

        super.updateForGameAsset(gameAsset);

        routineStage.loadFrom(gameAsset);
        variableCreationWindow.reloadWidgets();
        variableCreationWindow.setRoutineName(gameAsset.nameIdentifier);
    }

    @Override
    public String getAppName() {
        if(gameAsset == null) {
            return "Routine"; // lol wtf
        }
        return "Routine - " + gameAsset.nameIdentifier;
    }

    @Override
    public void onRemove() {
        // remove listeners and stuff somehow
    }

    @Override
    public void onUpdate() {
        variableCreationWindow.setRoutineName(gameAsset.nameIdentifier);
    }

}
