package com.talosvfx.talos.editor.project2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.layouts.LayoutGrid;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.notifications.events.FinishInitializingEvent;
import com.talosvfx.talos.editor.notifications.events.assets.MenuItemClickedEvent;
import com.talosvfx.talos.editor.widgets.ui.menu.MainMenu;

public class TalosControl implements Observer {

    public TalosControl() {
        Notifications.registerObserver(this);
    }


    @EventHandler
    public void onMenuItemClickedEvent(MenuItemClickedEvent event) {
        // TODO: switch this to some kind of reflection thing? or like map to instance thing *but how about startsWith things

        if(event.getPath().startsWith("file/open_recent")) {
            String path = (String) event.getPayload();
            TalosProjectData talosProjectData = TalosProjectData.loadFromFile(Gdx.files.internal(path));
            SharedResources.projectLoader.loadProject(talosProjectData);

            return;
        }

        if(event.getPath().equals("window/panels/close_all")) {
            SharedResources.appManager.closeAllFloatingWindows();
            return;
        } else if(event.getPath().startsWith("window/panels/")) {
            AppManager.BaseApp app = (AppManager.BaseApp) event.getPayload();
            return;
        }

        if(event.getPath().startsWith("window/apps/")) {
            Class<AppManager.BaseApp> clazz = (Class<AppManager.BaseApp>) event.getPayload();
            SharedResources.appManager.openApp(AppManager.singletonAsset, clazz);

            return;
        }

        if(event.getPath().startsWith("window/layouts/")) {
            String fileName = (String) event.getPayload();

            // murderous code here
            SharedResources.appManager.closeAllApps();

            LayoutGrid layoutGrid = SharedResources.currentProject.getLayoutGrid();
            JsonReader jsonReader = new JsonReader();
            JsonValue jsonValue = jsonReader.parse(Gdx.files.internal("layouts/" + fileName));
            layoutGrid.readFromJson(jsonValue);

            return;
        }
    }

    @EventHandler
    public void onFinishLoading(FinishInitializingEvent event) {
        loadLayoutGrids();
    }

    private void loadLayoutGrids() {
        XmlReader xmlReader = new XmlReader();
        final XmlReader.Element root = xmlReader.parse(Gdx.files.internal("layouts/layouts.xml"));

        SharedResources.mainMenu.registerMenuProvider(new MainMenu.IMenuProvider() {
            @Override
            public void inject(String path, MainMenu menu) {
                Array<XmlReader.Element> layouts = root.getChildrenByName("layout");
                for(XmlReader.Element layout : layouts) {
                    String fileName = layout.getAttribute("file");
                    String title = layout.getText();

                    menu.addItem(path, fileName, title, null, fileName);
                }
            }
        }, "window/layouts/list");

    }
}
