package com.talosvfx.talos.editor.project2.apps;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.talosvfx.talos.editor.addons.scene.apps.routines.ScenePreviewStage;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.apps.preferences.ContainerOfPrefs;
import com.talosvfx.talos.editor.project2.apps.preferences.ViewportPreferences;
import com.talosvfx.talos.editor.project2.localprefs.TalosLocalPrefs;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.scene.Scene;
import lombok.Getter;


@SingletonApp
public class ScenePreviewApp extends AppManager.BaseApp<Scene> implements GameAsset.GameAssetUpdateListener, Observer, ContainerOfPrefs<ViewportPreferences> {

    @Getter
    private final ScenePreviewStage workspaceWidget;

    public ScenePreviewApp() {
        Notifications.registerObserver(this);

        workspaceWidget = new ScenePreviewStage();
        workspaceWidget.disableListeners();

        DummyLayoutApp<Scene> sceneEditorWorkspaceApp = new DummyLayoutApp<Scene>(SharedResources.skin, this, getAppName()) {
            @Override
            public Actor getMainContent () {
                return workspaceWidget;
            }

            @Override
            public void onInputProcessorAdded () {
                super.onInputProcessorAdded();
                workspaceWidget.restoreListeners();
                SharedResources.stage.setScrollFocus(workspaceWidget);
            }

            @Override
            public void onInputProcessorRemoved () {
                super.onInputProcessorRemoved();
                workspaceWidget.disableListeners();
            }
        };

        this.gridAppReference = sceneEditorWorkspaceApp;
    }

    @Override
    public void updateForGameAsset(GameAsset<Scene> gameAsset) {
        super.updateForGameAsset(gameAsset);

        // TODO: 23.02.23 dummy refactor
        if (AppManager.dummyAsset == (GameAsset) gameAsset) {
            return;
        }

        workspaceWidget.setFromGameAsset(gameAsset);

        TalosLocalPrefs.getAppPrefs(gameAsset, this);
    }

    @Override
    public String getAppName() {
        if (gameAsset != null) {
            return "Preview - " + gameAsset.nameIdentifier;
        } else {
            return "Preview - ";
        }
    }

    @Override
    public void onRemove() {
        Notifications.unregisterObserver(this);
    }

    @Override
    public void onUpdate () {
        getGridAppReference().updateTabName(getAppName());
    }

    public void reload() {
        workspaceWidget.setFromGameAsset(gameAsset);
    }

    @Override
    public void applyFromPreferences(ViewportPreferences prefs) {
        workspaceWidget.applyPreferences(prefs);
    }

    @Override
    public ViewportPreferences getPrefs() {
        ViewportPreferences prefs = new ViewportPreferences();
        workspaceWidget.collectPreferences(prefs);
        return prefs;
    }

    public void setPaused(boolean paused) {
        this.workspaceWidget.setPaused(paused);
    }

    public void setSpeed(float timeScale) {
        workspaceWidget.setSpeed(timeScale);
    }

    public void setLockCamera(boolean checked) {
        workspaceWidget.setLockCamera(checked);
    }
}
