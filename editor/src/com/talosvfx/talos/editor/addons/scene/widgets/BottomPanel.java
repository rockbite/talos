package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.project2.SharedResources;

public class BottomPanel extends Table {

    private final Table leftTable;
    private final Table rightTable;

    public BottomPanel() {
        leftTable = new Table();
        rightTable = new Table();

        SplitPane splitPane = new SplitPane(leftTable, rightTable, false, SharedResources.skin, "timeline");
        splitPane.setSplitAmount(0.5f);

        add(splitPane).grow();
    }

    public void setWidgets(ProjectExplorerWidget projectExplorerWidget, HierarchyWidget hierarchyWidget) {
        leftTable.clearChildren();
        rightTable.clearChildren();

        leftTable.add(projectExplorerWidget).grow();
        rightTable.add(hierarchyWidget).grow();
    }
}
