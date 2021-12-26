package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;

public class ProjectExplorerWidget extends Table {

    private final FilteredTree<Object> directoryTree;

    public ProjectExplorerWidget() {

        Table leftTable = new Table();
        Table rightTable = new Table();

        directoryTree = new FilteredTree<>(TalosMain.Instance().getSkin(), "modern");
        ScrollPane scrollPane = new ScrollPane(directoryTree);
        leftTable.add(scrollPane).grow();

        SplitPane splitPane = new SplitPane(leftTable, rightTable, false, TalosMain.Instance().getSkin(), "timeline");
        splitPane.setSplitAmount(0.3f);

        add(splitPane).grow();
    }

    public void loadDirectoryTree (String path) {
        FileHandle root = Gdx.files.absolute(path);

        FilteredTree.Node rootNode = new FilteredTree.Node("project",  new Label("Project", TalosMain.Instance().getSkin()));
        directoryTree.add(rootNode);

        traversePath(root, 0, 3, rootNode);

        rootNode.setExpanded(true);
    }

    private void traversePath(FileHandle path, int currDepth, int maxDepth, FilteredTree.Node node) {
        if(path.isDirectory() && currDepth <= maxDepth) {
            FileHandle[] list = path.list();
            for(int i = 0; i < list.length; i++) {

                FileHandle dirHandle = list[i];

                RowWidget widget = new RowWidget(dirHandle);
                FilteredTree.Node newNode = new FilteredTree.Node(dirHandle.path(),  widget);
                node.add(newNode);

                traversePath(list[i], currDepth++, maxDepth, newNode);
            }
        } else {
            RowWidget widget = new RowWidget(path);
            FilteredTree.Node newNode = new FilteredTree.Node(path.path(),  widget);
            node.add(newNode);
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
