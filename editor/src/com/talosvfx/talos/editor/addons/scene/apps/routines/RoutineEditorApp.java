package com.talosvfx.talos.editor.addons.scene.apps.routines;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.data.RoutineStageData;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.notifications.CommandEventHandler;
import com.talosvfx.talos.editor.notifications.ContextRequiredEvent;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.notifications.commands.CommandContextType;
import com.talosvfx.talos.editor.notifications.commands.enums.Commands;
import com.talosvfx.talos.editor.notifications.events.commands.CommandContextEvent;
import com.talosvfx.talos.editor.notifications.events.commands.CommandEvent;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.apps.preferences.ContainerOfPrefs;
import com.talosvfx.talos.editor.project2.apps.preferences.ViewportPreferences;
import com.talosvfx.talos.editor.project2.localprefs.TalosLocalPrefs;
import com.talosvfx.talos.editor.project2.savestate.SaveSystem;
import com.talosvfx.talos.editor.project2.vfxui.GenericStageWrappedViewportWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoutineEditorApp extends AppManager.BaseApp<RoutineStageData> implements ContainerOfPrefs<ViewportPreferences>, GameAsset.GameAssetUpdateListener, Observer {

    public RoutineStage routineStage;
    public VariableCreationWindow variableCreationWindow;

    public GenericStageWrappedViewportWidget routineStageWrapper;

    private static final Logger logger = LoggerFactory.getLogger(RoutineEditorApp.class);

    public RoutineEditorApp() {
        Notifications.registerObserver(this);
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

        final Table content = new Table();
        Table separator = new Table();
        separator.setBackground(SharedResources.skin.newDrawable("white", Color.valueOf("#505050ff")));
        content.add(separator).growX().height(3).row();
        content.add(routineStageWrapper).grow();

        routineStage.init();

        variableCreationWindow = new VariableCreationWindow(routineStage);


        routineStageWrapper.left().top();
        routineStageWrapper.add(variableCreationWindow).pad(10).width(240);

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


            @Override
            protected void onTouchFocused () {
                SharedResources.stage.setKeyboardFocus(routineStageWrapper);
            }
        };

        this.gridAppReference = app;
    }

    @CommandEventHandler(commandType = Commands.CommandType.OPEN)
    public void onOpenCommand (CommandContextEvent event) {
        logger.info("ROUTINE CALLED OPEN");
    }

    @CommandEventHandler(commandType = Commands.CommandType.COPY)
    public void onCopyCommand (CommandContextEvent event) {
        logger.info("ROUTINE CALLED COPY");
        routineStage.getNodeBoard().copySelectedModules();
    }

    @CommandEventHandler(commandType = Commands.CommandType.PASTE)
    public void onPasteCommand (CommandContextEvent event) {
        logger.info("ROUTINE CALLED PASTE");
        routineStage.getNodeBoard().pasteFromClipboard();
    }

    @CommandEventHandler(commandType = Commands.CommandType.SELECT_ALL)
    public void onSelectAllCommand (CommandContextEvent event) {
        logger.info("ROUTINE CALLED SELECT ALL");
        routineStage.getNodeBoard().selectAllNodes();
    }

    @CommandEventHandler(commandType = Commands.CommandType.GROUP)
    public void onGroupCommand (CommandContextEvent event) {
        logger.info("ROUTINE CALLED GROUP");
        routineStage.getNodeBoard().createGroupFromSelectedNodes();
    }

    @CommandEventHandler(commandType = Commands.CommandType.UNGROUP)
    public void onUngroupCommand (CommandContextEvent event) {
        logger.info("ROUTINE CALLED UNGROUP");
        routineStage.getNodeBoard().ungroupSelectedNodes();
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
