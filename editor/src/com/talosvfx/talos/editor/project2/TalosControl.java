package com.talosvfx.talos.editor.project2;

import com.badlogic.gdx.Gdx;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.notifications.events.assets.MenuItemClickedEvent;

public class TalosControl implements Observer {

    public TalosControl() {
        Notifications.registerObserver(this);
    }


    @EventHandler
    public void onMenuItemClickedEvent(MenuItemClickedEvent event) {
        if(event.getPath().startsWith("file/open_recent")) {
            String path = (String) event.getPayload();
            TalosProjectData talosProjectData = TalosProjectData.loadFromFile(Gdx.files.internal(path));
            SharedResources.projectLoader.loadProject(talosProjectData);

            return;
        }

        if(event.getPath().equals("window/panels/close_all")) {
            SharedResources.appManager.closeAll();
        } else if(event.getPath().startsWith("window/panels/")) {
            AppManager.BaseApp app = (AppManager.BaseApp) event.getPayload();
        }
    }
}
