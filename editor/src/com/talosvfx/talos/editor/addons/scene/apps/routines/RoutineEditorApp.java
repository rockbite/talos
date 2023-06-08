package com.talosvfx.talos.editor.addons.scene.apps.routines;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.editor.addons.scene.apps.routines.ui.RoutineControlWindow;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.editor.data.RoutineStageData;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.CommandEventHandler;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.notifications.commands.enums.Commands;
import com.talosvfx.talos.editor.notifications.events.commands.CommandContextEvent;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.apps.preferences.ContainerOfPrefs;
import com.talosvfx.talos.editor.project2.apps.preferences.ViewportPreferences;
import com.talosvfx.talos.editor.project2.localprefs.TalosLocalPrefs;
import com.talosvfx.talos.editor.project2.vfxui.GenericStageWrappedViewportWidget;
import com.talosvfx.talos.editor.project2.vfxui.GenericStageWrappedWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoutineEditorApp extends AppManager.BaseApp<RoutineStageData> implements ContainerOfPrefs<ViewportPreferences>, GameAsset.GameAssetUpdateListener, Observer {
    public final RoutineControlWindow controlWindow;
    public RoutineStage routineStage;
    public VariableCreationWindow variableCreationWindow;

    public GenericStageWrappedViewportWidget routineStageWrapper;
    public GenericStageWrappedWidget routineUIStageWrapper;

    public Table uiContent;

    private static final Logger logger = LoggerFactory.getLogger(RoutineEditorApp.class);

    public RoutineEditorApp() {
        Notifications.registerObserver(this);
        routineStage = new RoutineStage(this, SharedResources.skin);
        routineStageWrapper = new GenericStageWrappedViewportWidget(routineStage.getRootActor()) {
            private static final float AUTO_SCROLL_RANGE = 45.0f;
            private static final float AUTO_SCROLL_SPEED = 200.0f;

            @Override
            protected boolean canMoveAround() {
                return true;
            }

            private Vector2 tmp = new Vector2();

            private static final float DELAY_BEFORE_MOVE = 0.3f;
            private float delayBeforeMove = DELAY_BEFORE_MOVE;

            @Override
            public void act(float delta) {
                super.act(delta);

                tmp.set(Gdx.input.getX(), Gdx.input.getY());
                screenToLocalCoordinates(tmp);

                float dt = Gdx.graphics.getDeltaTime();
                OrthographicCamera camera = (OrthographicCamera) routineStageWrapper.getViewportViewSettings().getCurrentCamera();

                tmp.set(Gdx.input.getX(), Gdx.input.getY());
                routineStageWrapper.screenToLocalCoordinates(tmp);

                boolean shouldMove = routineStage.shouldAutoMove()
                        && (isInTopZone(tmp) || isInBottomZone(tmp) || isInLeftZone(tmp) || isInRightZone(tmp));

                if (shouldMove) {
                    delayBeforeMove -= delta;
                } else {
                    delayBeforeMove = DELAY_BEFORE_MOVE;
                }

                if (delayBeforeMove < 0) {
                    if (isInTopZone(tmp)) {
                        camera.translate(0, camera.zoom * AUTO_SCROLL_SPEED * dt, 0);
                    } else if (isInBottomZone(tmp)) {
                        camera.translate(0, camera.zoom * -AUTO_SCROLL_SPEED * dt, 0);
                    }

                    if (isInLeftZone(tmp)) {
                        camera.translate(camera.zoom * AUTO_SCROLL_SPEED * dt, 0, 0);
                    } else if (isInRightZone(tmp)) {
                        camera.translate(camera.zoom * -AUTO_SCROLL_SPEED * dt, 0, 0);
                    }
                }

                routineStage.act();
            }


            private boolean isInRightZone(Vector2 mouse) {
                return mouse.x > 0 && mouse.x < AUTO_SCROLL_RANGE;
            }

            private boolean isInLeftZone(Vector2 mouse) {
                return mouse.x > routineStageWrapper.getWidth() - AUTO_SCROLL_RANGE && mouse.x < routineStageWrapper.getWidth();
            }

            private boolean isInBottomZone(Vector2 mouse) {
                return mouse.y > 0 && mouse.y < AUTO_SCROLL_RANGE;
            }

            private boolean isInTopZone(Vector2 mouse) {
                return mouse.y > routineStageWrapper.getHeight() - AUTO_SCROLL_RANGE && mouse.y < routineStageWrapper.getHeight();
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
            }

            @Override
            protected void onTouchFocused () {
                SharedResources.stage.setKeyboardFocus(routineStageWrapper);
            }

            @Override
            public void actInBackground(float delta) {
                super.actInBackground(delta);
                routineStage.act();
            }
        };

        this.gridAppReference = app;
    }

    @CommandEventHandler(commandType = Commands.CommandType.COPY)
    public void onCopyCommand (CommandContextEvent event) {
        routineStage.getNodeBoard().copySelectedModules();
    }

    @CommandEventHandler(commandType = Commands.CommandType.PASTE)
    public void onPasteCommand (CommandContextEvent event) {
        routineStage.getNodeBoard().pasteFromClipboard();
    }

    @CommandEventHandler(commandType = Commands.CommandType.SELECT_ALL)
    public void onSelectAllCommand (CommandContextEvent event) {
        routineStage.getNodeBoard().selectAllNodes();
    }

    @CommandEventHandler(commandType = Commands.CommandType.GROUP)
    public void onGroupCommand (CommandContextEvent event) {
        routineStage.getNodeBoard().createGroupFromSelectedNodes();
    }

    @CommandEventHandler(commandType = Commands.CommandType.UNGROUP)
    public void onUngroupCommand (CommandContextEvent event) {
        routineStage.getNodeBoard().ungroupSelectedNodes();
    }

    @CommandEventHandler(commandType = Commands.CommandType.DELETE)
    public void onDeleteCommand (CommandContextEvent event) {
        routineStage.getNodeBoard().deleteSelectedNodes();
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

        // TODO: 23.02.23 dummy refactor
        if (AppManager.dummyAsset == (GameAsset) gameAsset) {
            return;
        }

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

        if (this.gameAsset != null) {
            this.gameAsset.listeners.removeValue(this, true);
        }

    }

    @Override
    public void onUpdate() {
        variableCreationWindow.setRoutineName(gameAsset.nameIdentifier);
        routineStage.loadFrom(gameAsset);
    }

    public void applyFromPreferences(ViewportPreferences prefs) {
        routineStageWrapper.applyPreferences(prefs);
    }

    @Override
    public ViewportPreferences getPrefs() {
        ViewportPreferences prefs = new ViewportPreferences();
        routineStageWrapper.collectPreferences(prefs);
        return prefs;
    }
}
