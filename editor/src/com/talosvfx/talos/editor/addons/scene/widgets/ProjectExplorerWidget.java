package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Selection;
import com.badlogic.gdx.utils.*;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.events.AssetColorFillEvent;
import com.talosvfx.talos.editor.addons.scene.logic.TilePaletteData;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.addons.scene.widgets.directoryview.DirectoryViewWidget;
import com.talosvfx.talos.editor.addons.scene.widgets.directoryview.KeepStopReplaceDialog;
import com.talosvfx.talos.editor.dialogs.YesNoDialog;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.notifications.events.DirectoryChangedEvent;
import com.talosvfx.talos.editor.notifications.events.ProjectLoadedEvent;
import com.talosvfx.talos.editor.notifications.events.assets.AssetChangeDirectoryEvent;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.TalosProjectData;
import com.talosvfx.talos.runtime.utils.NamingUtils;
import com.talosvfx.talos.editor.widgets.ui.ActorCloneable;
import com.talosvfx.talos.editor.widgets.ui.ContextualMenu;
import com.talosvfx.talos.editor.widgets.ui.EditableLabel;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;
import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.interfaces.StringSimilarity;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

import static com.talosvfx.talos.editor.utils.InputUtils.ctrlPressed;

public class ProjectExplorerWidget extends Table implements Observer {

    private static final Logger logger = LoggerFactory.getLogger(ProjectExplorerWidget.class);

    private final FilteredTree<String> directoryTree;
    public static FileFilter fileFilter;
    private final DirectoryViewWidget directoryViewWidget;
    private ObjectMap<String, FilteredTree.Node<String>> nodes = new ObjectMap<>();

    private ContextualMenu contextualMenu;
    private FilteredTree.Node<String> rootNode;

    public boolean isCutting = false;
    public Array<FileHandle> filesToManipulate = new Array<>();

    public ProjectExplorerWidget() {
        Skin skin = SharedResources.skin;
        Notifications.registerObserver(this);

        setBackground(SharedResources.skin.newDrawable("white", Color.valueOf("#282828ff")));

        Table horizontalPanel = new Table();
        horizontalPanel.setBackground(SharedResources.skin.newDrawable("white", Color.valueOf("#505050ff")));
        add(horizontalPanel).growX().row();

        SearchWidget searchWidget = new SearchWidget();
        horizontalPanel.add(searchWidget).pad(2).expandX().right();

        TextField searchField = searchWidget.getTextField();
        searchField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String text = searchField.getText().toLowerCase();
                if (text.length() == 0) {
                    directoryViewWidget.openDirectory(getCurrentFolder().path());
                    return;
                }
                FileHandle root = SharedResources.currentProject.rootProjectDir();
                Array<FileHandle> stack = new Array<>();
                Array<FileHandle> similarFiles = new Array<>();
                StringSimilarity similar = new JaroWinkler();

                for (FileHandle fileHandle : root.list()) {
                    stack.add(fileHandle);
                }

                while (!stack.isEmpty()) {
                    FileHandle file = stack.pop();
                    if (file.name().toLowerCase().contains(text)) {
                        similarFiles.add(file);
                    } else if (similar.similarity(text, file.nameWithoutExtension().toLowerCase()) > 0.9) {
                        similarFiles.add(file);
                    }

                    if (file.isDirectory()) {
                        for (FileHandle child : file.list()) {
                            stack.add(child);
                        }
                    }
                }
                directoryViewWidget.fillItems(similarFiles);
            }
        });

        contextualMenu = new ContextualMenu();

        Table container = new Table();

        directoryTree = new FilteredTree<>(SharedResources.skin, "modern");

        Table directoryTreeTable = new Table();
        directoryTreeTable.top().defaults().top();

        directoryTreeTable.add(directoryTree).growX();

        ScrollPane scrollPane = new ScrollPane(directoryTreeTable);

        Table scrollPaneTable = new Table();
        scrollPaneTable.top().defaults().top();

        scrollPaneTable.add(scrollPane).height(0).grow();

        directoryViewWidget = new DirectoryViewWidget(this);
        SplitPane splitPane = new SplitPane(scrollPaneTable, directoryViewWidget, false, SharedResources.skin);
        splitPane.setSplitAmount(0.35f);

        container.add(splitPane).grow();
        add(container).grow().padTop(3);

        directoryTree.draggable = true;

        fileFilter = new FileFilter() {
            @Override
            public boolean accept (File pathname) {

                if(pathname.getAbsolutePath().endsWith(".tse")) return false;
                if(pathname.getName().equals(".DS_Store")) return false;
                if(pathname.getAbsolutePath().endsWith(".meta")) return false;
                if(pathname.getAbsolutePath().endsWith(".p")) return false;

                return true;
            }
        };

        directoryTree.addItemListener(new FilteredTree.ItemListener<String>() {

            @Override
            public void onNodeMove (FilteredTree.Node<String> parentToMoveTo, FilteredTree.Node<String> childThatHasMoved, int indexInParent, int indexOfPayloadInPayloadBefore) {
                if(parentToMoveTo != null) {
                    String parentPath = parentToMoveTo.getObject();
                    String childPath = childThatHasMoved.getObject();

                    System.out.println("Moving " + childPath + " " + " to " + parentPath);

                    FileHandle parentHandle = Gdx.files.absolute(parentPath);
                    FileHandle childHandle = Gdx.files.absolute(childPath);


                    //Its always folders

                    AssetRepository.getInstance().moveFile(childHandle, parentHandle, false);

                }
            }

            @Override
            public void selected (FilteredTree.Node node) {
                select(node);
                directoryViewWidget.openDirectory((String) node.getObject());
            }

            @Override
            public void addedIntoSelection (FilteredTree.Node<String> node) {
                super.addedIntoSelection(node);
            }

            @Override
            public void rightClick (FilteredTree.Node<String> node) {
                if(node != null) {
                    //select(node);
                    Array<FileHandle> contextualFile = new Array<>();
                    contextualFile.add(Gdx.files.absolute(node.getObject()));

                    showContextMenu(contextualFile, false);
                }
            }

            @Override
            public void delete (Array<FilteredTree.Node<String>> nodes) {
                if (nodes.isEmpty()) return;

                String path = (String) nodes.first().getObject();
                Array<String> paths = new Array<>();
                paths.add(path);
                deletePath(paths);
            }
        });

        directoryTree.expandAll();
    }

    private String getCurrSelectedPath() {
        Selection<FilteredTree.Node<String>> selection = directoryTree.getSelection();
        if(selection.size() > 0) {
            return (String) selection.first().getObject();
        }

        return null;
    }


    public void deletePath(Array<String> paths) {
        Runnable runnable = new Runnable() {
            @Override
            public void run () {
                if (paths.isEmpty()) {
                    // nothing no remove
                    return;
                }

                FileHandle parent = null;

                for (String path : paths) {
                    FileHandle handle = Gdx.files.absolute(path);
                    AssetImporter.deleteFile(handle);

                    parent = handle.parent();
                }

                loadDirectoryTree(rootNode.getObject());

                if(!parent.path().equals(parent)) {
                    expand(parent.path());
                    select(parent.path());
                }
            }
        };

		String pathString = "";
		for (String path : paths) {
			pathString += path + "\n";
		}

        showYesNoDialog("Delete files?", "Are you sure you want to delete the paths: \n" + pathString, runnable, new Runnable() {
            @Override
            public void run () {

            }
        });
    }

    public  ObjectMap<String, FilteredTree.Node<String>> getNodes(){
        return nodes;
    }

    public void showContextMenu (boolean directoryView) {
        Array<FileHandle> list = new Array<>();
        Array<FilteredTree.Node<String>> nodes = directoryTree.getSelection().toArray();
        for (FilteredTree.Node<String> node: nodes) {
            String path = node.getObject();
            FileHandle handle = Gdx.files.absolute(path);
            list.add(handle);
        }

        showContextMenu(list, directoryView);
    }

    public void showContextMenu (Array<FileHandle> files, boolean directory) {
        contextualMenu.clearItems();

        contextualMenu.addItem("Cut", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                invokeCut(files);
            }
        });
        contextualMenu.addItem("Copy", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                invokeCopy(files);
            }
        });
        contextualMenu.addItem("Paste", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                FileHandle destination = files.first();
                invokePaste(destination);
            }
        });
        contextualMenu.addSeparator();
        contextualMenu.addItem("Rename", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                String path = files.first().path();
                if(path != null) {
                    FileHandle handle = Gdx.files.absolute(path);
                    if (directory) {
                        directoryViewWidget.rename();
                    } else {
                        if (handle.isDirectory()) {
                            if (nodes.get(path) != null) {
                                RowWidget widget = (RowWidget) nodes.get(path).getActor();
                                widget.label.setEditMode();
                            }
                        }
                    }
                }
            }
        });
        contextualMenu.addItem("Delete", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                String path = files.first().path();
                Array<String> paths = new Array<>();
                for(FileHandle file: files) {
                    paths.add(file.path());
                }
                deletePath(paths);
            }
        });


        if(files.size == 1 && files.first().isDirectory()) {
            contextualMenu.addSeparator();

            PopupMenu popupMenu = new PopupMenu();

            createSubMenuItem(popupMenu, "New Directory", new ClickListener() {
                @Override
                public void clicked (InputEvent event, float x, float y) {
                    String path = files.first().path();
                    if (path != null) {
                        FileHandle handle = Gdx.files.absolute(path);
                        if (handle.isDirectory()) {
                            String name = NamingUtils.getNewName("New Directory", new Supplier<Collection<String>>() {
                                @Override
                                public Collection<String> get () {
                                    FileHandle[] list = handle.list();
                                    ArrayList<String> names = new ArrayList<>();
                                    for (FileHandle fileHandle : list) {
                                        names.add(fileHandle.name());
                                    }
                                    return names;
                                }
                            });
                            FileHandle newHandle = handle.child(name);

                            newHandle.mkdirs();
                            loadDirectoryTree(rootNode.getObject());
                            FilteredTree.Node newNode = nodes.get(newHandle.path());
                            expand(newHandle.path());
                            select(newNode.getParent());
                            // TODO: refactor directory view widget to update itself
                            select(getCurrentFolder().path());
//                            directoryViewWidgetNew.startRenameFor(newHandle);

                            directoryViewWidget.selectForPath(newHandle);
                            directoryViewWidget.rename();

                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run () {
                                    directoryViewWidget.scrollTo(newHandle);
                                }
                            });
                        }
                    }
                }
            });

            createSubMenuItem(popupMenu, "New Scene", new ClickListener() {
                @Override
                public void clicked (InputEvent event, float x, float y) {
                    FileHandle currentFolder = getCurrentFolder();
                    FileHandle sceneFileHandle = AssetRepository.getInstance().copySampleSceneToProject(currentFolder);
                    // TODO: refactor directory view widget to update itself
                    select(getCurrentFolder().path());

                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            directoryViewWidget.scrollTo(sceneFileHandle);
                        }
                    });
                }
            });

            createSubMenuItem(popupMenu, "Particle Effect", new ClickListener() {
                @Override
                public void clicked (InputEvent event, float x, float y) {

                    FileHandle currentFolder = getCurrentFolder();
                    FileHandle effectFileHandle = AssetRepository.getInstance().copySampleParticleToProject(currentFolder);

                    // TODO: refactor directory view widget to update itself
                    select(getCurrentFolder().path());

                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run () {
                            directoryViewWidget.scrollTo(effectFileHandle);
                        }
                    });
                }
            });

            createSubMenuItem(popupMenu, "Script", new ClickListener() {
                @Override
                public void clicked (InputEvent event, float x, float y) {

                    FileHandle currentFolder = getCurrentFolder();

                    FileHandle newScriptDestination = AssetImporter.suggestNewNameForFileHandle(currentFolder.path(), "New_Script", "ts");
                    FileHandle templateScript = Gdx.files.internal("addons/scene/missing/ScriptTemplate.ts");

                    String templateString = templateScript.readString();
                    templateString = templateString.replaceAll("%TEMPLATE_NAME%", newScriptDestination.nameWithoutExtension());
                    newScriptDestination.writeString(templateString, false);

                    AssetRepository.getInstance().rawAssetCreated(newScriptDestination, true);


                    // TODO: refactor directory view widget to update itself
                    select(getCurrentFolder().path());

                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run () {
                            directoryViewWidget.scrollTo(newScriptDestination);
                        }
                    });
                }
            });

            createSubMenuItem(popupMenu, "Routine", new ClickListener() {
                @Override
                public void clicked (InputEvent event, float x, float y) {

                    FileHandle currentFolder = getCurrentFolder();

                    FileHandle newScriptDestination = AssetImporter.suggestNewNameForFileHandle(currentFolder.path(), "Routine", "rt");
                    newScriptDestination.writeString("{}", false);

                    AssetRepository.getInstance().rawAssetCreated(newScriptDestination, true);


                    // TODO: refactor directory view widget to update itself
                    select(getCurrentFolder().path());

                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run () {
                            directoryViewWidget.scrollTo(newScriptDestination);
                        }
                    });
                }
            });

            createSubMenuItem(popupMenu, "Palette", new ClickListener() {
                @Override
                public void clicked (InputEvent event, float x, float y) {
                    FileHandle currentFolder = getCurrentFolder();
                    FileHandle newPaletteDestination = AssetImporter.suggestNewNameForFileHandle(currentFolder.path(), "New_Palette", "ttp");

                    Json json = new Json(JsonWriter.OutputType.json);
                    String templateString = json.toJson(new TilePaletteData());
                    newPaletteDestination.writeString(templateString, false);

                    AssetRepository.getInstance().rawAssetCreated(newPaletteDestination, true);


                    // TODO: refactor directory view widget to update itself
                    select(getCurrentFolder().path());

                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run () {
                            directoryViewWidget.scrollTo(newPaletteDestination);
                        }
                    });
                }
            });

            createSubMenuItem(popupMenu, "Empty Sprite PNG", new ClickListener() {
                @Override
                public void clicked (InputEvent event, float x, float y) {

                    FileHandle currentFolder = getCurrentFolder();
                    FileHandle destination = AssetImporter.suggestNewNameForFileHandle(currentFolder.path(), "new-sprite", "png");

                    Pixmap pixmap = new Pixmap(100, 100, Pixmap.Format.RGBA8888);
                    PixmapIO.writePNG(destination, pixmap);
                    pixmap.dispose();

                    AssetRepository.getInstance().rawAssetCreated(destination, true);
                    select(getCurrentFolder().path());
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run () {
                            directoryViewWidget.scrollTo(destination);
                        }
                    });
                }
            });



            MenuItem createMenu = contextualMenu.addItem("Create", new ClickListener() {
                @Override
                public void clicked (InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                }
            });

            createMenu.setSubMenu(popupMenu);
        }


        contextualMenu.show(getStage());
    }

    private void createSubMenuItem(PopupMenu popupMenu, String name, ClickListener listener) {
        MenuItem item = new MenuItem(name);
        item.addListener(listener);
        popupMenu.addItem(item);
    }

    public void select (FilteredTree.Node node) {
        if (ctrlPressed()){
            directoryTree.getSelection().add(node);
        }else {
            directoryTree.getSelection().clear();
            directoryTree.getSelection().add(node);
            directoryViewWidget.openDirectory((String) node.getObject());
        }
    }

    public void select (String path) {
        if (nodes.containsKey(path)) {
            directoryTree.getSelection().clear();
            directoryTree.getSelection().add(nodes.get(path));
            expand(path);
            String pathToSet = (String)nodes.get(path).getObject();
            directoryViewWidget.openDirectory(pathToSet);
        } else {
            directoryTree.getSelection().clear();
            directoryTree.getSelection().add(rootNode);
            expand(path);
            directoryViewWidget.openDirectory(path);
        }
    }

    public void expand() {
        directoryTree.expandAll();
    }

    public void expand(String path) {
        if(nodes.containsKey(path)) {
            nodes.get(path).setExpanded(true);
            FilteredTree.Node parent = nodes.get(path).getParent();
            while(parent != null) {
                parent.setExpanded(true);
                parent = parent.getParent();
            }
        }
    }

    public void loadDirectoryTree (String path) {
        nodes.clear();
        directoryTree.clearChildren();
        FileHandle root = Gdx.files.absolute(path);

        rootNode = new FilteredTree.Node("project",  new Label("Project", SharedResources.skin));
        rootNode.setObject(path); // project path
        traversePath(root, 0, 10, rootNode);
        directoryTree.add(rootNode);

        rootNode.canDelete = false;

        rootNode.setExpanded(true);

        rootNode.expandAll();

        directoryViewWidget.openDirectory(root.path());
    }

    private void traversePath(FileHandle path, int currDepth, int maxDepth, FilteredTree.Node node) {
        if(path.isDirectory() && currDepth <= maxDepth) {
            FileHandle[] list = path.list(fileFilter);
            for (int i = 0; i < list.length; i++) {
                FileHandle listItemHandle = list[i];

                if(!listItemHandle.isDirectory()) continue;

                RowWidget widget = new RowWidget(listItemHandle);
                EditableLabel label = widget.getLabel();
                label.getTextField().setTextFieldFilter(AssetRepository.ASSET_NAME_FIELD_FILTER);
                final FilteredTree.Node newNode = new FilteredTree.Node(listItemHandle.path(),  widget);

                if (listItemHandle.isDirectory()) {
                    if (listItemHandle.name().equals("assets") || listItemHandle.name().equals("scenes")) {
                        newNode.canDelete = false;
                    }
                }

                newNode.setObject(listItemHandle.path());
                newNode.draggable = true;
                node.add(newNode);
                nodes.put(listItemHandle.path(), newNode);
                if(listItemHandle.isDirectory()) {
                    traversePath(list[i], currDepth++, maxDepth, newNode);
                }

                label.setListener(new EditableLabel.EditableLabelChangeListener() {
                    @Override
                    public void editModeStarted () {

                    }

                    @Override
                    public void changed (String newText) {
                        String path = (String) newNode.getObject();
                        FileHandle fileHandle = Gdx.files.absolute(path);

                        if(newText.isEmpty()) {
                            newText = fileHandle.nameWithoutExtension();
                        }

                        FileHandle newHandle = AssetImporter.renameFile(fileHandle, newText);

                        notifyRename(fileHandle, newHandle);
                    }
                });
            }
        }
    }

    public void notifyRename(FileHandle old, FileHandle newFile) {
        FilteredTree.Node node = nodes.get(old.path());
        if(node != null) {
            node.setObject(newFile.path());
            nodes.remove(old.path());
            nodes.put(newFile.path(), node);

            RowWidget widget = (RowWidget) node.getActor();
            widget.set(newFile);
        }
    }

    public FileHandle getCurrentFolder () {
        return directoryViewWidget.getCurrentFolder();
    }

    public void reload () {
        // TODO: refactor directory view widget to update itself
        select(getCurrentFolder().path());
    }

    public static class RowWidget extends Table implements ActorCloneable<RowWidget> {
        private final EditableLabel label;
        private FileHandle fileHandle;
        private final boolean editable;

        public RowWidget(FileHandle fileHandle) {
            this(fileHandle, true);
        }

        public RowWidget(FileHandle fileHandle, boolean editable) {
            this.fileHandle = fileHandle;
            this.editable = editable;
            Image icon;
            if(fileHandle.isDirectory()) {
                icon = new Image(SharedResources.skin.getDrawable("ic-folder"));
            } else {
                icon = new Image(SharedResources.skin.getDrawable("ic-file-blank"));
            }

            label = new EditableLabel(fileHandle.name(), SharedResources.skin);
            label.setEditable(editable);

            add(icon);
            add(label).growX().padLeft(5).width(250);
        }

        public void set(FileHandle fileHandle) {
            this.fileHandle = fileHandle;
            label.setText(fileHandle.name());
        }

        public FileHandle getFileHandle(){
            return fileHandle;
        }

        public EditableLabel getLabel() {
            return label;
        }

        @Override
        public RowWidget copyActor(RowWidget copyFrom) {
            RowWidget widget = new RowWidget(fileHandle, editable);
            return widget;
        }
    }


    public void invokeCut(Array<FileHandle> files) {
        filesToManipulate.clear();
        filesToManipulate.addAll(files);
        isCutting = true;
    }

    public void invokeCopy(Array<FileHandle> files) {
        filesToManipulate.clear();
        filesToManipulate.addAll(files);
        isCutting = false;
    }

    public void invokePaste(FileHandle destination) {
        if(destination.isDirectory()) {
            for(FileHandle file: filesToManipulate) {
                if(isCutting) {
                    AssetImporter.moveFile(file, destination, false);
                } else {
                    AssetImporter.copyFile(file, destination);
                }
            }
        }

        filesToManipulate.clear();

        loadDirectoryTree((String) rootNode.getObject());

        expand(destination.path());
        select(destination.path());
    }

    public DirectoryViewWidget getDirectoryViewWidget() {
        return directoryViewWidget;
    }

    @EventHandler
    public void onProjectLoad (ProjectLoadedEvent event) {
        TalosProjectData projectData = event.getProjectData();
        loadDirectoryTree(projectData.rootProjectDir().path());
    }

    @EventHandler
    public void onDirectoryContentsChanged (DirectoryChangedEvent event) {
        if(getCurrentFolder().path().equals(event.getDirectoryPath())) {
            reload();
        }
    }

    @EventHandler
    public void onDirectoryChange (AssetChangeDirectoryEvent assetChangeDirectoryEvent) {
        select(assetChangeDirectoryEvent.getPath().path());
    }

    @EventHandler
    public void onAssetColorFillEvent (AssetColorFillEvent event) {
        directoryViewWidget.changeAssetPreview(event.getFileHandle());
    }

    public void showYesNoDialog (String title, String message, Runnable yes, Runnable no) {
		YesNoDialog yesNoDialog = new YesNoDialog(title, message, yes, no);
		getStage().addActor(yesNoDialog.fadeIn());
	}

    public void showKeepStopReplaceDialog (String title, String message, Runnable keep, Runnable stop, Runnable replace) {
        KeepStopReplaceDialog keepStopReplaceDialog = new KeepStopReplaceDialog(title, message, keep, stop, replace);
        getStage().addActor(keepStopReplaceDialog.fadeIn());
    }
}
