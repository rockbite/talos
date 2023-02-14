package com.talosvfx.talos.editor.project2;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.ProjectLoadedEvent;
import com.talosvfx.talos.editor.notifications.events.ProjectUnloadEvent;
import com.talosvfx.talos.editor.project2.localprefs.TalosLocalPrefs;
import com.talosvfx.talos.editor.socket.SocketServer;
import com.talosvfx.talos.runtime.RuntimeContext;
import lombok.Setter;

public class ProjectLoaderImpl implements ProjectLoader {

    @Setter
    private Table layoutGridContainer;

    @Override
    public void loadProject(TalosProjectData projectData) {
        if (layoutGridContainer != null) {
            SharedResources.currentProject = projectData;
            RuntimeContext.getInstance().setSceneData(projectData.getSceneData());

            TalosLocalPrefs.Instance().updateProject(projectData);

            layoutGridContainer.clearChildren();
            layoutGridContainer.add(projectData.getLayoutGrid()).grow();

            ProjectLoadedEvent projectLoadedEvent = Notifications.obtainEvent(ProjectLoadedEvent.class);
            projectLoadedEvent.setProjectData(projectData);
            Notifications.fireEvent(projectLoadedEvent);

            projectData.loadLayout();

            //todo: move this somewhere else
            SocketServer.getInstance();
        }
    }

    @Override
    public void unloadProject() {
        if (layoutGridContainer != null) {
            SharedResources.appManager.removeAll();

            layoutGridContainer.clearChildren();

            RuntimeContext.getInstance().setSceneData(null);
            SharedResources.currentProject = null;

            Notifications.quickFire(ProjectUnloadEvent.class);
        }
    }
}
