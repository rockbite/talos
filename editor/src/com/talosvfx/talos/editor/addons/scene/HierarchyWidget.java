package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;

public class HierarchyWidget extends Table {

    private FilteredTree tree;

    public HierarchyWidget() {
        tree = new FilteredTree<>(TalosMain.Instance().getSkin(), "modern");

        FilteredTree.Node parent = new FilteredTree.Node("screen", new Label("Screen", TalosMain.Instance().getSkin()));
        tree.add(parent);


        for(int i = 0; i < 9; i++) {
            FilteredTree.Node child = new FilteredTree.Node("entity_0" + i, new Label("Entity 0" + i, TalosMain.Instance().getSkin()));
            parent.add(child);
        }

        tree.expandAll();

        add(tree).grow().pad(5).padRight(0);
    }
}
