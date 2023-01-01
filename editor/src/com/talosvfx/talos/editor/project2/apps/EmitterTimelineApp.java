package com.talosvfx.talos.editor.project2.apps;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.ParticleEmitterWrapper;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.events.vfx.VFXEditorActivated;
import com.talosvfx.talos.editor.addons.scene.events.vfx.VFXPreviewActivated;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.serialization.VFXProjectData;
import com.talosvfx.talos.editor.widgets.ui.EmitterList;
import com.talosvfx.talos.editor.widgets.ui.ModuleBoardWidget;
import com.talosvfx.talos.editor.wrappers.ModuleWrapper;

public class EmitterTimelineApp extends AppManager.BaseApp<VFXProjectData> implements Observer {

    private EmitterList emitterList;

    public EmitterTimelineApp() {
        singleton = true;

        Notifications.registerObserver(this);

        emitterList = new EmitterList(SharedResources.skin);

        this.gridAppReference = new DummyLayoutApp(SharedResources.skin, getAppName()) {
            @Override
            public Actor getMainContent () {
                return emitterList;
            }
            @Override
            public void onInputProcessorAdded () {
                super.onInputProcessorAdded();

            }
            @Override
            public void onInputProcessorRemoved () {
                super.onInputProcessorRemoved();
            }
        };
    }

    @Override
    public void updateForGameAsset (GameAsset<VFXProjectData> gameAsset) {
        super.updateForGameAsset(gameAsset);

        loadFromCurrentlyActiveEditor();
        setDataFromPreview();
    }

    @EventHandler
    public void onVFXEditorActivated(VFXEditorActivated event) {
        if(event.asset == gameAsset) {
            loadFromCurrentlyActiveEditor();
        }
    }

    @EventHandler
    public void onVFXPreviewActivated(VFXPreviewActivated event) {
        if(event.asset == gameAsset) {
            setDataFromPreview();
        }
    }

    private void setDataFromPreview() {
        ParticlePreviewApp app = SharedResources.appManager.getAppForAsset(ParticlePreviewApp.class, gameAsset);

        if(app != null) {
           emitterList.setPreview(app.getPreview3D());
        }
    }

    private void loadFromCurrentlyActiveEditor() {
        ParticleNodeEditorApp app = SharedResources.appManager.getAppForAsset(ParticleNodeEditorApp.class, gameAsset);

        if(app != null) {
            ObjectMap<ParticleEmitterWrapper, Array<ModuleWrapper>> moduleWrappers = app.getModuleBoardWidget().moduleWrappers;
            Array<ParticleEmitterWrapper> particleEmitterWrappers = moduleWrappers.keys().toArray();
            emitterList.setEmitters(particleEmitterWrappers);
            emitterList.setEditorApp(app);
        }
    }

    @Override
    public String getAppName () {
        return "VFX Emitters";
    }

    @Override
    public void onRemove () {

    }

}
