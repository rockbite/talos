package com.talosvfx.talos.editor.project2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.util.dialog.OptionDialogListener;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.notifications.events.assets.AssetChangeDirectoryEvent;
import com.talosvfx.talos.editor.project2.localprefs.TalosLocalPrefs;
import com.talosvfx.talos.editor.utils.Toasts;
import com.talosvfx.talos.editor.addons.scene.events.save.SaveRequest;
import com.talosvfx.talos.editor.addons.scene.events.save.ExportRequest;
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
           SharedResources.mainMenu.registerMenuProvider(new MainMenu.IMenuProvider() {
               @Override
               public void inject(String path, MainMenu menu) {
                   Array<String> customLayouts = TalosLocalPrefs.Instance().getCustomLayouts();

                   for(String customLayoutPath : customLayouts) {
                       FileHandle handle = Gdx.files.absolute(customLayoutPath);
                       menu.addItem(path, handle.path(), handle.name(), null, handle);
                   }
               }
           }, "window/layouts/custom_list");
       }
    }

    @EventHandler
    public void onMenuItemClickedEvent(MenuItemClickedEvent event) {
        // TODO: switch this to some kind of reflection thing? or like map to instance thing *but how about startsWith things
        // or at least start delegating this into methods

        if(event.getPath().equals("file/open")) {
            openProjectByChoosingFile();
            return;
        }

        if(event.getPath().startsWith("file/open_recent")) {
            String path = (String) event.getPayload();
            FileHandle handle = new FileHandle(path);

            if (SharedResources.appManager.hasChangesToSave()) {
                SharedResources.appManager.requestConfirmationToCloseWithoutSave(new OptionDialogListener() {
                    @Override
                    public void yes() {
                        validateAndOpenProject(handle);
                    }

                    @Override
                    public void no() {
                        validateAndOpenProject(handle);
                    }

                    @Override
                    public void cancel() {
                        // do nothing
                    }
                });
            } else {
                validateAndOpenProject(handle);
            }

            return;
        }

        if (event.getPath().equals("file/new/project")) {
            if (SharedResources.appManager.hasChangesToSave()) {
                SharedResources.appManager.requestConfirmationToCloseWithoutSave(new OptionDialogListener() {
                    @Override
                    public void yes() {
                        SharedResources.appManager.removeAll();
                        SharedResources.projectLoader.unloadProject();
                        ProjectSplash projectSplash = new ProjectSplash();
                        projectSplash.show(SharedResources.stage);
                    }

                    @Override
                    public void no() {
                        SharedResources.appManager.removeAll();
                        SharedResources.projectLoader.unloadProject();
                        ProjectSplash projectSplash = new ProjectSplash();
                        projectSplash.show(SharedResources.stage);
                    }

                    @Override
                    public void cancel() {
                        // do nothing
                    }
                });
            } else {
                SharedResources.appManager.removeAll();
                SharedResources.projectLoader.unloadProject();
                ProjectSplash projectSplash = new ProjectSplash();
                projectSplash.show(SharedResources.stage);
            }
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

        if(event.getPath().equals("file/export/project")) {
            Notifications.quickFire(ExportRequest.class);
        }

        if(event.getPath().equals("file/save")) {
            Notifications.quickFire(SaveRequest.class);
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
            String jsonValue = SharedResources.currentProject.getCurrentJsonLayoutRepresentation();

            String ext = GameAssetType.LAYOUT_DATA.getExtensions().first();
            FileSystemInteraction.instance().showSaveFileChooser(ext, new FileChooserListener() {
                @Override
                public void selected(Array<FileHandle> files) {
                    FileHandle file = files.first();
                    if (!file.extension().equals("." + ext)) {
                        file = file.parent().child(file.nameWithoutExtension() + "." + ext);
                    }
                    // use file chooser to get the file path
                    file.writeString(jsonValue, false);

                    TalosLocalPrefs.Instance().addCustomLayout(file.path());
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
            if (SharedResources.appManager.hasChangesToSave()) {
                SharedResources.appManager.requestConfirmationToCloseWithoutSave(new OptionDialogListener() {
                    @Override
                    public void yes() {
                        Gdx.app.exit();
                    }

                    @Override
                    public void no() {
                        Gdx.app.exit();
                    }

                    @Override
                    public void cancel() {
                        // do nothing
                    }
                });
            } else {
                Gdx.app.exit();
            }
        }

        if (event.getPath().startsWith("help")) {
            if (event.getPath().endsWith("about")) {
                SharedResources.ui.showAboutTalosDialog();
            }
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

                        // change to file's directory
                        AssetChangeDirectoryEvent assetChangeDirectoryEvent = Notifications.obtainEvent(AssetChangeDirectoryEvent.class);
                        assetChangeDirectoryEvent.setPath(destination.parent());
                        Notifications.fireEvent(assetChangeDirectoryEvent);
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
    }

    public void openProjectByChoosingFile() {
        openProjectByChoosingFile(null);
    }

    public void openProjectByChoosingFile(Runnable after) {
        FileSystemInteraction.instance().showFileChooser("tlsprj", new FileChooserListener() {
            @Override
            public void selected(Array<FileHandle> files) {
                if (SharedResources.appManager.hasChangesToSave()) {
                    SharedResources.appManager.requestConfirmationToCloseWithoutSave(new OptionDialogListener() {
                        @Override
                        public void yes() {
                            boolean success = validateAndOpenProject(files.first());
                            if (success) {
                                if(after != null) {
                                    after.run();
                                }
                            }
                        }

                        @Override
                        public void no() {
                            boolean success = validateAndOpenProject(files.first());
                            if (success) {
                                if(after != null) {
                                    after.run();
                                }
                            }
                        }

                        @Override
                        public void cancel() {
                            // do nothing
                        }
                    });
                } else {
                    boolean success = validateAndOpenProject(files.first());
                    if (success) {
                        if(after != null) {
                            after.run();
                        }
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
                return validateAndOpenProject(talosProjectData);
            } else {
                Dialogs.showErrorDialog(SharedResources.stage, "No valid project found to load");
            }
        } else {
            Dialogs.showErrorDialog(SharedResources.stage, "No valid project found to load");
        }

        return false;

    }

    private boolean validateAndOpenProject (TalosProjectData talosProjectData) {
        try {
            // unload old project, before loading new one
            SharedResources.projectLoader.unloadProject();

            SharedResources.projectLoader.loadProject(talosProjectData);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
