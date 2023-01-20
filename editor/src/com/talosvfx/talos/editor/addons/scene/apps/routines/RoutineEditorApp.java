package com.talosvfx.talos.editor.addons.scene.apps.routines;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.editor.addons.scene.apps.routines.ui.RoutineControlWindow;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.editor.data.RoutineStageData;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.apps.preferences.ContainerOfPrefs;
import com.talosvfx.talos.editor.project2.apps.preferences.ViewportPreferences;
import com.talosvfx.talos.editor.project2.localprefs.TalosLocalPrefs;
import com.talosvfx.talos.editor.project2.vfxui.GenericStageWrappedViewportWidget;
import com.talosvfx.talos.editor.project2.vfxui.GenericStageWrappedWidget;

public class RoutineEditorApp extends AppManager.BaseApp<RoutineStageData> implements ContainerOfPrefs<ViewportPreferences>, GameAsset.GameAssetUpdateListener {

    public final RoutineControlWindow controlWindow;
    public RoutineStage routineStage;
    public VariableCreationWindow variableCreationWindow;

    public GenericStageWrappedViewportWidget routineStageWrapper;
    public GenericStageWrappedWidget routineUIStageWrapper;

    public Table uiContent;

    public RoutineEditorApp() {
        routineStage = new RoutineStage(this, SharedResources.skin);
        routineStageWrapper = new GenericStageWrappedViewportWidget(routineStage.getRootActor()) {

            @Override
            protected boolean canMoveAround() {
                return true;
            }

            @Override
            public void act(float delta) {
                super.act(delta);

                routineStage.act();
            }
        };
        routineStageWrapper.getDropdownForWorld().setVisible(false);

        uiContent = new Table();
        uiContent.setFillParent(true);
        routineUIStageWrapper = new GenericStageWrappedWidget(uiContent);

        final Table content = new Table();
        Table separator = new Table();
        separator.setBackground(SharedResources.skin.newDrawable("white", Color.valueOf("#505050ff")));
        content.add(separator).growX().height(3).row();
        Stack stack = new Stack(routineStageWrapper, routineUIStageWrapper);
        content.add(stack).grow();

        routineStage.init();

        variableCreationWindow = new VariableCreationWindow(routineStage);
        uiContent.add(variableCreationWindow).pad(10).width(240).left().top().expand();

        controlWindow = new RoutineControlWindow(routineStage);
        uiContent.add(controlWindow).pad(10).right().top().expand();

        routineStage.sendInStage(routineStageWrapper.getStage());

        DummyLayoutApp<RoutineStageData> app = new DummyLayoutApp<RoutineStageData>(SharedResources.skin, this, getAppName()) {
            @Override
            public Actor getMainContent() {
                return content;
            }

            @Override
            public void onInputProcessorAdded () {
                super.onInputProcessorAdded();
                routineStageWrapper.restoreListeners();
                SharedResources.stage.setScrollFocus(routineStageWrapper);
                SharedResources.inputHandling.addPriorityInputProcessor(routineUIStageWrapper.getStage());
                SharedResources.inputHandling.addPriorityInputProcessor(routineStageWrapper.getStage());
                SharedResources.inputHandling.setGDXMultiPlexer();
            }

            @Override
            public void onInputProcessorRemoved () {
                super.onInputProcessorRemoved();
                routineStageWrapper.disableListeners();
                SharedResources.inputHandling.removePriorityInputProcessor(routineUIStageWrapper.getStage());
                SharedResources.inputHandling.removePriorityInputProcessor(routineStageWrapper.getStage());
                SharedResources.inputHandling.setGDXMultiPlexer();

                Stage stage = routineStageWrapper.getStage();
            }


            @Override
            protected void onTouchFocused () {
                // if mouse hit coordinates are in uni focus on ui else on stage
                final Vector2 coords = new Vector2(Gdx.input.getX(), Gdx.input.getY());
                uiContent.screenToLocalCoordinates(coords);
                if (uiContent.hit(coords.x, coords.y, true) != null) {
                    routineStageWrapper.getStage().unfocusAll();
                    SharedResources.stage.setScrollFocus(routineUIStageWrapper);
                } else {
                    routineUIStageWrapper.getStage().unfocusAll();
                    SharedResources.stage.setScrollFocus(routineStageWrapper);
                }
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
        TalosLocalPrefs.getAppPrefs(gameAsset, this);

        routineStage.loadFrom(gameAsset);
        variableCreationWindow.reloadWidgets();
        variableCreationWindow.setRoutineName(gameAsset.nameIdentifier);

        controlWindow.update();
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
        routineStageWrapper.disableListeners();
        SharedResources.inputHandling.removePriorityInputProcessor(routineUIStageWrapper.getStage());
        SharedResources.inputHandling.removePriorityInputProcessor(routineStageWrapper.getStage());
        SharedResources.inputHandling.setGDXMultiPlexer();

        Notifications.unregisterObserver(routineStage);

    }

    @Override
    public void onUpdate() {
        variableCreationWindow.setRoutineName(gameAsset.nameIdentifier);
    }

    public void applyFromPreferences(ViewportPreferences prefs) {
        routineStageWrapper.setCameraPos(prefs.cameraPos);
        routineStageWrapper.setCameraZoom(prefs.cameraZoom);
    }

    @Override
    public ViewportPreferences getPrefs() {
        ViewportPreferences prefs = new ViewportPreferences();
        prefs.cameraPos = routineStageWrapper.getCameraPos();
        prefs.cameraZoom = routineStageWrapper.getCameraZoom();
        return prefs;
    }
}
