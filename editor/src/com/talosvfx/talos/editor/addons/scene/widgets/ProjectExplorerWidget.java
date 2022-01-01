package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Selection;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.widgets.ui.ContextualMenu;
import com.talosvfx.talos.editor.widgets.ui.EditableLabel;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;

import java.io.File;
import java.io.FileFilter;

public class ProjectExplorerWidget extends Table {

    private final FilteredTree<Object> directoryTree;
    public static FileFilter fileFilter;
    private ObjectMap<String, FilteredTree.Node> nodes = new ObjectMap<>();

    private ContextualMenu contextualMenu;
    private FilteredTree.Node rootNode;

    public ProjectExplorerWidget() {
        contextualMenu = new ContextualMenu();

        directoryTree = new FilteredTree<>(TalosMain.Instance().getSkin(), "modern");
        ScrollPane scrollPane = new ScrollPane(directoryTree);
        add(scrollPane).grow().padTop(3);

        fileFilter = new FileFilter() {
            @Override
            public boolean accept (File pathname) {

                if(pathname.getAbsolutePath().endsWith(".tse")) return false;
                if(pathname.getName().equals(".DS_Store")) return false;

                return true;
            }
        };

        directoryTree.setItemListener(new FilteredTree.ItemListener() {
            @Override
            public void rightClick (FilteredTree.Node node) {
                select(node);
                showContextMenu();
            }

            @Override
            public void delete (Array<FilteredTree.Node> nodes) {
                String path = (String) nodes.first().getObject();
                deletePath(path);
            }
        });

        directoryTree.expandAll();
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

    private void deletePath(String path) {
        if(path != null) {
            String rootPath = (String) rootNode.getObject();
            if(path.equals(rootPath)) return;
            if(!path.startsWith(rootPath)) return;
            if(path.equals(rootPath + File.separator + "assets")) return;
            if(path.equals(rootPath + File.separator + "scenes")) return;

            FileHandle handle = Gdx.files.absolute(path);
            if(handle.exists()) {
                FileHandle parent = handle.parent();

                deleteRecursively(handle);

                loadDirectoryTree((String) rootNode.getObject());
                if(!parent.path().equals(rootPath)) {
                    expand(parent.path());
                    select(parent.path());
                }
            }
        }
    }

    private void deleteRecursively(FileHandle handle) {
        if(handle.isDirectory()) {
            FileHandle[] list = handle.list();
            if (list.length > 0) {
                for (int i = 0; i < list.length; i++) {
                    deleteRecursively(list[i]);
                }
            }
        }

        handle.delete();
    }

    private void showContextMenu () {
        contextualMenu.clearItems();
        contextualMenu.addItem("New Directory", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                String path = getCurrSelectedPath();
                if(path != null) {
                    FileHandle handle = Gdx.files.absolute(path);
                    if(handle.isDirectory()) {
                        FileHandle newHandle  = findAvailableHandleIn(handle, "New Directory");
                        newHandle.mkdirs();
                        loadDirectoryTree((String) rootNode.getObject());
                        FilteredTree.Node newNode = nodes.get(newHandle.path());
                        expand(newHandle.path());
                        select(newNode);
                        RowWidget widget = (RowWidget) newNode.getActor();
                        EditableLabel label = widget.getLabel();
                        label.setEditMode();
                    }
                }
            }
        });
        contextualMenu.addSeparator();
        contextualMenu.addItem("Cut", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {

            }
        });
        contextualMenu.addItem("Copy", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {

            }
        });
        contextualMenu.addItem("Paste", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {

            }
        });
        contextualMenu.addSeparator();
        contextualMenu.addItem("Rename", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                String path = getCurrSelectedPath();
                if(path != null) {
                    RowWidget widget = (RowWidget) nodes.get(path).getActor();
                    widget.label.setEditMode();;
                }
            }
        });
        contextualMenu.addItem("Delete", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                String path = getCurrSelectedPath();
                deletePath(path);
            }
        });
        contextualMenu.show(getStage());
    }

    public void select (FilteredTree.Node node) {
        directoryTree.getSelection().clear();
        directoryTree.getSelection().add(node);
    }

    public void select (String path) {
        if(nodes.containsKey(path)) {
            directoryTree.getSelection().clear();
            directoryTree.getSelection().add(nodes.get(path));
            expand(path);
        }
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

        rootNode = new FilteredTree.Node("project",  new Label("Project", TalosMain.Instance().getSkin()));
        rootNode.setObject(path); // project path
        directoryTree.add(rootNode);

        traversePath(root, 0, 10, rootNode);

        rootNode.setExpanded(true);
    }

    private void traversePath(FileHandle path, int currDepth, int maxDepth, FilteredTree.Node node) {
        if(path.isDirectory() && currDepth <= maxDepth) {
            FileHandle[] list = path.list(fileFilter);
            for(int i = 0; i < list.length; i++) {
                FileHandle listItemHandle = list[i];

                RowWidget widget = new RowWidget(listItemHandle);
                EditableLabel label = widget.getLabel();
                final FilteredTree.Node newNode = new FilteredTree.Node(listItemHandle.path(),  widget);
                newNode.setObject(listItemHandle.path());
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

                        if(!fileHandle.isDirectory()) {
                            String extension = fileHandle.extension();
                            if (!newText.contains(".")) newText += "." + extension;
                        }

                        FileHandle parent = fileHandle.parent();
                        FileHandle newHandle = Gdx.files.absolute(parent.path() + File.separator + newText);
                        fileHandle.moveTo(newHandle);

                        newNode.setObject(newHandle.path());
                        nodes.remove(path);
                        nodes.put(newHandle.path(), newNode);
                    }
                });
            }
        }
    }

    public static class RowWidget extends Table {
        private final EditableLabel label;

        public RowWidget(FileHandle fileHandle) {
            this(fileHandle, true);
        }

        public RowWidget(FileHandle fileHandle, boolean editable) {
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

        public EditableLabel getLabel() {
            return label;
        }
    }
}
