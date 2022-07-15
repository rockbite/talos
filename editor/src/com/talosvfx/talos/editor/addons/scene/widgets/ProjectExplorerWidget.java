package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.Selection;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.kotcrab.vis.ui.widget.VisSplitPane;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.logic.Scene;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.TlsMetadata;
import com.talosvfx.talos.editor.widgets.ui.ActorCloneable;
import com.talosvfx.talos.editor.widgets.ui.ContextualMenu;
import com.talosvfx.talos.editor.widgets.ui.EditableLabel;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;

import java.io.File;
import java.io.FileFilter;

public class ProjectExplorerWidget extends Table {

    private final FilteredTree<Object> directoryTree;
    public static FileFilter fileFilter;
    private final DirectoryViewWidget directoryViewWidget;
    private ObjectMap<String, FilteredTree.Node> nodes = new ObjectMap<>();

    private ContextualMenu contextualMenu;
    private FilteredTree.Node rootNode;

    private DragAndDrop dragAndDrop;

    public boolean isCutting = false;
    public Array<FileHandle> filesToManipulate = new Array<>();

    public ProjectExplorerWidget() {
        contextualMenu = new ContextualMenu();

        Table container = new Table();

        directoryTree = new FilteredTree<>(TalosMain.Instance().getSkin(), "modern");
        ScrollPane scrollPane = new ScrollPane(directoryTree);

        directoryViewWidget = new DirectoryViewWidget();
        ScrollPane scrollPaneRight = new ScrollPane(directoryViewWidget);
        scrollPaneRight.setScrollingDisabled(true, false);

        VisSplitPane splitPane = new VisSplitPane(scrollPane, scrollPaneRight, false);
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

                return true;
            }
        };

        directoryTree.setItemListener(new FilteredTree.ItemListener<Object>() {
            @Override
            public void selected (FilteredTree.Node node) {
                directoryViewWidget.setDirectory((String) node.getObject());
            }

            @Override
            public void chosen (FilteredTree.Node node) {
                directoryViewWidget.setDirectory((String) node.getObject());
            }

            @Override
            public void rightClick (FilteredTree.Node node) {
                if(node != null) {
                    select(node);
                    showContextMenu(false);
                }
            }

            @Override
            public void delete (Array<FilteredTree.Node<Object>> nodes) {
                String path = (String) nodes.first().getObject();
                Array<String> paths = new Array<>();
                paths.add(path);
                deletePath(paths);
            }
        });

        directoryTree.expandAll();

        dragAndDrop = new DragAndDrop();
    }

    private String getCurrSelectedPath() {
        Selection<FilteredTree.Node<Object>> selection = directoryTree.getSelection();
        if(selection.size() > 0) {
            return (String) selection.first().getObject();
        }

        return null;
    }

    private FileHandle findAvailableHandleIn(FileHandle parent, String name) {
        int i = 0;
        String testName = name;
        FileHandle newHandle = Gdx.files.absolute(parent.path() + File.separator + testName);
        while(newHandle.exists()) {
            testName = name + " (" + (++i) + ")";
            newHandle = Gdx.files.absolute(parent.path() + File.separator + testName);
        }
        // if this loop continues forever, scream "sacrebleu" reevaluate mistakes you did in life, and overflow.

        return newHandle;
    }

    public void deletePath(Array<String> paths) {
        FileHandle parent = SceneEditorAddon.get().workspace.getProjectFolder();
        for(String path: paths) {
            FileHandle handle = Gdx.files.absolute(path);
            AssetImporter.deleteFile(handle);

            parent = handle.parent();
        }

        loadDirectoryTree((String) rootNode.getObject());

        if(!parent.path().equals(parent)) {
            expand(parent.path());
            select(parent.path());
        }
    }

    public void showContextMenu (boolean directoryView) {
        Array<FileHandle> list = new Array<>();
        Array<FilteredTree.Node<Object>> nodes = directoryTree.getSelection().toArray();
        for (FilteredTree.Node<Object> node: nodes) {
            String path = (String) node.getObject();
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
                    if(handle.isDirectory()) {
                        if (directory) {
                            directoryViewWidget.startRenameFor(handle);
                        } else if (nodes.get(path) != null) {
                            RowWidget widget = (RowWidget) nodes.get(path).getActor();
                            widget.label.setEditMode();
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
                            FileHandle newHandle = findAvailableHandleIn(handle, "New Directory");
                            newHandle.mkdirs();
                            loadDirectoryTree((String) rootNode.getObject());
                            FilteredTree.Node newNode = nodes.get(newHandle.path());
                            expand(newHandle.path());
                            select(newNode.getParent());
                            RowWidget widget = (RowWidget) newNode.getActor();
                            directoryViewWidget.reload();
                            directoryViewWidget.startRenameFor(newHandle);
                        }
                    }
                }
            });

            createSubMenuItem(popupMenu, "New Scene", new ClickListener() {
                @Override
                public void clicked (InputEvent event, float x, float y) {
                    String path = files.first().path();
                    FileHandle sceneDestination = AssetImporter.suggestNewName(path, "New Scene", "scn");
                    Scene mainScene = new Scene(sceneDestination.path());
                    mainScene.save();
                    directoryViewWidget.reload();
                }
            });

            createSubMenuItem(popupMenu, "Particle Effect", new ClickListener() {
                @Override
                public void clicked (InputEvent event, float x, float y) {
                    String path = files.first().path();
                    FileHandle tlsDestination = AssetImporter.suggestNewName(path, "New Effect", "tls");
                    FileHandle metadataHandle = AssetImporter.makeSimilar(tlsDestination,"meta");
                    FileHandle originalTls = Gdx.files.internal("addons/scene/missing/sample.tls");
                    originalTls.copyTo(tlsDestination);
                    TlsMetadata metadata = new TlsMetadata();
                    metadata.tlsChecksum = AssetImporter.checkSum(tlsDestination);
                    AssetImporter.saveMetadata(metadataHandle, metadata);
                    directoryViewWidget.reload();
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
        directoryTree.getSelection().clear();
        directoryTree.getSelection().add(node);
        directoryViewWidget.setDirectory((String) node.getObject());
    }

    public void select (String path) {
        if(nodes.containsKey(path)) {
            directoryTree.getSelection().clear();
            directoryTree.getSelection().add(nodes.get(path));
            expand(path);
            String pathToSet = (String)nodes.get(path).getObject();
            directoryViewWidget.setDirectory(pathToSet);
        } else {
            directoryTree.getSelection().clear();
            directoryTree.getSelection().add(rootNode);
            expand(path);
            directoryViewWidget.setDirectory(path);
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
        dragAndDrop.clear();

        nodes.clear();
        directoryTree.clearChildren();
        FileHandle root = Gdx.files.absolute(path);

        rootNode = new FilteredTree.Node("project",  new Label("Project", TalosMain.Instance().getSkin()));
        rootNode.setObject(path); // project path
        traversePath(root, 0, 10, rootNode);
        directoryTree.add(rootNode);

        rootNode.setExpanded(true);

        rootNode.expandAll();

        directoryViewWidget.setDirectory(root.path());
        directoryViewWidget.reload();
    }

    private void traversePath(FileHandle path, int currDepth, int maxDepth, FilteredTree.Node node) {
        if(path.isDirectory() && currDepth <= maxDepth) {
            FileHandle[] list = path.list(fileFilter);
            for(int i = 0; i < list.length; i++) {
                FileHandle listItemHandle = list[i];

                if(!listItemHandle.isDirectory()) continue;

                RowWidget widget = new RowWidget(listItemHandle);
                EditableLabel label = widget.getLabel();
                final FilteredTree.Node newNode = new FilteredTree.Node(listItemHandle.path(),  widget);
                newNode.setObject(listItemHandle.path());
                //newNode.draggable = true; // todo: for later file manipulation
                node.add(newNode);
                nodes.put(listItemHandle.path(), newNode);
                if(listItemHandle.isDirectory()) {
                    traversePath(list[i], currDepth++, maxDepth, newNode);
                }

                label.setListener(new EditableLabel.EditableLabelChangeListener() {
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
        directoryViewWidget.reload();
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
                icon = new Image(TalosMain.Instance().getSkin().getDrawable("ic-folder"));
            } else {
                icon = new Image(TalosMain.Instance().getSkin().getDrawable("ic-file-blank"));
            }

            label = new EditableLabel(fileHandle.name(), TalosMain.Instance().getSkin());
            label.setEditable(editable);

            add(icon);
            add(label).growX().padLeft(5).width(250);
        }

        public void set(FileHandle fileHandle) {
            this.fileHandle = fileHandle;
            label.setText(fileHandle.name());
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
                    AssetImporter.moveFile(file, destination);
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
}
