package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.widgets.ui.ContextualMenu;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;

import java.io.File;
import java.io.FileFilter;

public class ProjectExplorerWidget extends Table {

    private final FilteredTree<Object> directoryTree;
    private final FileFilter fileFilter;
    private ObjectMap<String, FilteredTree.Node> nodes = new ObjectMap<>();

    private ContextualMenu contextualMenu;

    public ProjectExplorerWidget() {
        contextualMenu = new ContextualMenu();

        directoryTree = new FilteredTree<>(TalosMain.Instance().getSkin(), "modern");
        ScrollPane scrollPane = new ScrollPane(directoryTree);
        add(scrollPane).grow();

        fileFilter = new FileFilter() {
            @Override
            public boolean accept (File pathname) {

                if(pathname.getAbsolutePath().endsWith(".tse")) return false;

                return true;
            }
        };

        directoryTree.setItemListener(new FilteredTree.ItemListener() {
            @Override
            public void rightClick (FilteredTree.Node node) {
                select(node);
                showContextMenu();
            }
        });

        directoryTree.expandAll();
    }

    private void showContextMenu () {
        contextualMenu.clearItems();
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

            }
        });
        contextualMenu.addItem("Delete", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {

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

        FilteredTree.Node rootNode = new FilteredTree.Node("project",  new Label("Project", TalosMain.Instance().getSkin()));
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
                FilteredTree.Node newNode = new FilteredTree.Node(listItemHandle.path(),  widget);
                node.add(newNode);
                nodes.put(listItemHandle.path(), newNode);
                if(listItemHandle.isDirectory()) {
                    traversePath(list[i], currDepth++, maxDepth, newNode);
                }
            }
        }
    }

    public class RowWidget extends Table {
        public RowWidget(FileHandle fileHandle) {
            Image icon;
            if(fileHandle.isDirectory()) {
                icon = new Image(TalosMain.Instance().getSkin().getDrawable("ic-folder"));
            } else {
                icon = new Image(TalosMain.Instance().getSkin().getDrawable("ic-file-blank"));
            }

            Label label = new Label(fileHandle.name(), TalosMain.Instance().getSkin());
            label.setEllipsis(true);

            add(icon);
            add(label).growX().padLeft(5).width(250);
        }
    }
}
