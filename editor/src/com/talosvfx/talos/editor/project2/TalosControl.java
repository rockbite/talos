package com.talosvfx.talos.editor.project2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.utils.Toasts;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.filesystem.FileChooserListener;
import com.talosvfx.talos.editor.filesystem.FileSystemInteraction;
import com.talosvfx.talos.editor.layouts.LayoutGrid;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.notifications.events.FinishInitializingEvent;
import com.talosvfx.talos.editor.notifications.events.MenuPopupOpenCommand;
import com.talosvfx.talos.editor.notifications.events.assets.GameAssetOpenEvent;
import com.talosvfx.talos.editor.notifications.events.assets.MenuItemClickedEvent;
import com.talosvfx.talos.editor.widgets.ui.menu.MainMenu;
import com.talosvfx.talos.runtime.maps.TilePaletteData;

import java.util.function.Consumer;

import static com.talosvfx.talos.editor.layouts.LayoutGrid.LayoutJsonStructure;

import static com.talosvfx.talos.editor.project2.TalosProjectData.TALOS_PROJECT_EXTENSION;

public class TalosControl implements Observer {

    private MainMenu.IMenuProvider customLayoutListProvider;

    public TalosControl() {
        Notifications.registerObserver(this);
    }


    @EventHandler
    public void onMenuPopupOpenCommand (MenuPopupOpenCommand menuPopupOpenCommand) {
       if (menuPopupOpenCommand.getPath().equals("window/layouts")) {
            SharedResources.mainMenu.askToInject(customLayoutListProvider, "window/layouts/custom_list");
       }
    }

    @EventHandler
    public void onMenuItemClickedEvent(MenuItemClickedEvent event) {
        // TODO: switch this to some kind of reflection thing? or like map to instance thing *but how about startsWith things
        // or at least start delegating this into methods

        if(event.getPath().startsWith("file/open")) {
            openProjectByChoosingFile();
            return;
        }

        if(event.getPath().startsWith("file/open_recent")) {
            String path = (String) event.getPayload();
            TalosProjectData talosProjectData = TalosProjectData.loadFromFile(Gdx.files.internal(path));
            SharedResources.projectLoader.loadProject(talosProjectData);

            return;
        }

        if(event.getPath().equals("file/new/routine")) {
            // create routine
            askToSaveFile("rt", (newScriptDestination) -> newScriptDestination.writeString("{}", false));
        } else if (event.getPath().equals("file/new/vfx")) {
            // create vfx
            askToSaveFile("tls", (newParticleDestination) -> {
                FileHandle effectFileHandle = AssetRepository.getInstance().copySampleParticleToProject(newParticleDestination.parent());
                AssetRepository.getInstance().moveFile(effectFileHandle, newParticleDestination, false, true);
            });
        } else if (event.getPath().equals("file/new/script")) {
            // create script
            askToSaveFile("ts", (newScriptDestination) -> {
                FileHandle templateScript = Gdx.files.internal("addons/scene/missing/ScriptTemplate.ts");

                String templateString = templateScript.readString();
                templateString = templateString.replaceAll("%TEMPLATE_NAME%", newScriptDestination.nameWithoutExtension());
                newScriptDestination.writeString(templateString, false);
            });
        } else if (event.getPath().equals("file/new/palette")) {
            // palette
            askToSaveFile("ttp", (newPaletteDestination) -> {
                Json json = new Json(JsonWriter.OutputType.json);
                String templateString = json.toJson(new TilePaletteData());
                newPaletteDestination.writeString(templateString, false);
            });
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

                        AssetRepository.getInstance().rawAssetCreated(destination, true);
                    } else {
                        // show error message
                    }
                }
            });
        } else {
            if (event.getPath().startsWith("window/layouts/")) {
                FileHandle handle = (FileHandle) event.getPayload();
                // murderous code here
                SharedResources.appManager.removeAll();

                LayoutGrid layoutGrid = SharedResources.currentProject.getLayoutGrid();
                JsonReader jsonReader = new JsonReader();
                JsonValue jsonValue = jsonReader.parse(handle);
                layoutGrid.readFromJson(jsonValue);

                return;
            }
        }

        if (event.getPath().startsWith("edit")) {
            if (event.getPath().endsWith("preferences")) {

                SharedResources.ui.showPreferencesWindow();

                return;
            }
        }

        if(event.getPath().equals("file/quit")) {
            Gdx.app.exit();
        }
    }

    private void askToSaveFile (String extension, Consumer<FileHandle> saveCallback) {
        FileSystemInteraction.instance().showSaveFileChooser(extension, new FileChooserListener() {
            @Override
            public void selected(Array<FileHandle> files) {
                if (files.size == 1) {
                    FileHandle target = files.first();
                    if (target.isDirectory()) {
                        return;
                    }
                    if (!target.extension().equals(extension)) {
                        target = target.parent().child(target.nameWithoutExtension() + "." + extension);
                    }
                    if (target.nameWithoutExtension().trim().equals("")) {
                        target = target.parent().child("untitled." + extension);
                    }
                    if (SharedResources.currentProject.isPathInsideProject(target.path())) {
                        FileHandle destination = AssetImporter.suggestNewNameForFileHandle(target.parent().path(), target.nameWithoutExtension(), extension);

                        // use destination to save the file
                        saveCallback.accept(destination);

                        AssetRepository.getInstance().rawAssetCreated(destination, true);
                    } else {
                        Toasts.getInstance().showErrorToast("Path doesn't belong to the project. Didn't save!");
                    }
                }
            }
        });
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

                    menu.addItem(path, fileName, title, null, Gdx.files.internal("layouts/" + fileName));
                }
            }
        }, "window/layouts/list");

        // load custom layout lists
        customLayoutListProvider = new MainMenu.IMenuProvider() {
            @Override
            public void inject(String path, MainMenu menu) {
                Array<GameAsset<LayoutJsonStructure>> layouts = AssetRepository.getInstance().getAssetsForType(GameAssetType.LAYOUT_DATA);
                for (GameAsset<LayoutJsonStructure> layout : layouts) {
                    FileHandle handle = layout.getRootRawAsset().handle;
                    menu.addItem(path, handle.name(), handle.nameWithoutExtension(), null, handle);
                }
            }
        };
        SharedResources.mainMenu.registerMenuProvider(customLayoutListProvider, "window/layouts/custom_list");

    }

    public void openProjectByChoosingFile() {
        openProjectByChoosingFile(null);
    }

    public void openProjectByChoosingFile(Runnable after) {
        FileSystemInteraction.instance().showFileChooser("tlsprj", new FileChooserListener() {
            @Override
            public void selected(Array<FileHandle> files) {
                boolean success = validateAndOpenProject(files.first());
                if (success) {
                    if(after != null) {
                        after.run();
                    }
                }
            }
        });
    }

    public boolean validateAndOpenProject (FileHandle first) {
        FileHandle projectToTryToLoad = null;
        if (first.isDirectory()) {
            FileHandle[] list = first.list();
            for (FileHandle handle : list) {
                if (handle.extension().equals(TALOS_PROJECT_EXTENSION)) {
                    projectToTryToLoad = handle;
                    break;
                }
            }
        } else {
            if (first.extension().equals(TALOS_PROJECT_EXTENSION)) {
                projectToTryToLoad = first;
            }
        }

        if (projectToTryToLoad != null) {
            TalosProjectData talosProjectData = TalosProjectData.loadFromFile(projectToTryToLoad);
            if (talosProjectData != null) {
                try {
                    SharedResources.projectLoader.loadProject(talosProjectData);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                Dialogs.showErrorDialog(SharedResources.stage, "No valid project found to load");
            }
        } else {
            Dialogs.showErrorDialog(SharedResources.stage, "No valid project found to load");
        }

        return false;

    }

    @EventHandler
    public void onGameAssetOpenEvent(GameAssetOpenEvent event) {
        if (event.getGameAsset().type == GameAssetType.LAYOUT_DATA) {
            GameAsset<LayoutJsonStructure> gameAsset = (GameAsset<LayoutJsonStructure>) event.getGameAsset();
            // murderous code here
            SharedResources.appManager.removeAll();

            LayoutGrid layoutGrid = SharedResources.currentProject.getLayoutGrid();
            layoutGrid.readFromJsonStructure(gameAsset.getResource());
        }
    }
}
