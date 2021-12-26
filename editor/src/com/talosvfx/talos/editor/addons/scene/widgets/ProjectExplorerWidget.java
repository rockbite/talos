package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;
import com.talosvfx.talos.runtime.script.ScriptCompiler;

import java.io.File;
import java.io.FileFilter;

public class ProjectExplorerWidget extends Table {

    private final FilteredTree<Object> directoryTree;
    private final FileFilter fileFilter;
    private ObjectMap<String, FilteredTree.Node> nodes = new ObjectMap<>();

    public ProjectExplorerWidget() {

        Table leftTable = new Table();
        Table rightTable = new Table();

        directoryTree = new FilteredTree<>(TalosMain.Instance().getSkin(), "modern");
        ScrollPane scrollPane = new ScrollPane(directoryTree);
        leftTable.add(scrollPane).grow();

        SplitPane splitPane = new SplitPane(leftTable, rightTable, false, TalosMain.Instance().getSkin(), "timeline");
        splitPane.setSplitAmount(0.3f);

        add(splitPane).grow();

        fileFilter = new FileFilter() {
            @Override
            public boolean accept (File pathname) {

                if(pathname.getAbsolutePath().endsWith(".tse")) return false;

                return true;
            }
        };
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
