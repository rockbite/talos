package com.talosvfx.talos.editor.project2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.filesystem.FileChooserListener;
import com.talosvfx.talos.editor.filesystem.FileSystemInteraction;
import com.talosvfx.talos.editor.layouts.LayoutGrid;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.notifications.events.FinishInitializingEvent;
import com.talosvfx.talos.editor.notifications.events.assets.MenuItemClickedEvent;
import com.talosvfx.talos.editor.widgets.ui.menu.MainMenu;

import java.io.StringWriter;

public class TalosControl implements Observer {

    private MainMenu.IMenuProvider customLayoutListProvider;

    public TalosControl() {
        Notifications.registerObserver(this);
    }


    @EventHandler
    public void onMenuItemClickedEvent(MenuItemClickedEvent event) {
        // TODO: switch this to some kind of reflection thing? or like map to instance thing *but how about startsWith things
        // or at least start delegating this into methods

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

        if(event.getPath().equals("window/layouts/save_layout")) {
            JsonValue jsonValue = SharedResources.currentProject.getJsonLayoutRepresentation();
            String data = jsonValue.toJson(JsonWriter.OutputType.json);

            String ext = GameAssetType.LAYOUT_DATA.getExtensions().first();
            FileSystemInteraction.instance().showSaveFileChooser(ext, new FileChooserListener() {
                @Override
                public void selected(Array<FileHandle> files) {
                    FileHandle file = files.first();
                    boolean pathInsideProject = SharedResources.currentProject.isPathInsideProject(file.path());
                    if(pathInsideProject) {
                        // use file chooser to get the file path
                        FileHandle destination = AssetImporter.suggestNewNameForFileHandle(file.parent().path(), file.nameWithoutExtension(), ext);
                        destination.writeString(data, false);

                        AssetRepository.getInstance().rawAssetCreated(destination, false);

                        SharedResources.mainMenu.askToInject(customLayoutListProvider, "window/layouts/custom_list");
                    } else {
                        // show error message
                    }
                }
            });
        } else {
            if (event.getPath().startsWith("window/layouts/")) {
                // todo: also add custom ones here
                String fileName = (String) event.getPayload();

                // murderous code here
                SharedResources.appManager.removeAll();

                LayoutGrid layoutGrid = SharedResources.currentProject.getLayoutGrid();
                JsonReader jsonReader = new JsonReader();
                JsonValue jsonValue = jsonReader.parse(Gdx.files.internal("layouts/" + fileName));
                layoutGrid.readFromJson(jsonValue);

                return;
            }
        }

        if (event.getPath().startsWith("edit")) {
            if (event.getPath().endsWith("preferences")) {
                System.out.println("magic");
                SharedResources.ui.showPreferencesWindow();
                return;
            }
        }

        if(event.getPath().equals("file/quit")) {
            Gdx.app.exit();
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

        // load custom layout lists
        customLayoutListProvider = new MainMenu.IMenuProvider() {
            @Override
            public void inject(String path, MainMenu menu) {
                //AssetRepository.getInstance().
                // find all files with .tlslt extension, and load their names here
            }
        };
        SharedResources.mainMenu.registerMenuProvider(customLayoutListProvider, "window/layouts/custom_list");

    }
}
