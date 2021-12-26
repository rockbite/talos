package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.GameObjectContainer;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;

public class HierarchyWidget extends Table {

    private FilteredTree tree;

    public HierarchyWidget() {
        tree = new FilteredTree<>(TalosMain.Instance().getSkin(), "modern");

        add(tree).grow().pad(5).padRight(0);
    }

    public void loadEntityContainer(GameObjectContainer entityContainer) {
        tree.clearChildren();

        FilteredTree.Node parent = new FilteredTree.Node("root", new Label(entityContainer.getName(), TalosMain.Instance().getSkin()));
        tree.add(parent);

        traverseEntityContainer(entityContainer, parent);

        tree.expandAll();
    }

    private void traverseEntityContainer(GameObjectContainer entityContainer, FilteredTree.Node node) {
        Array<GameObject> gameObjects = entityContainer.getGameObjects();

        if(gameObjects == null) return;

        for(int i = 0; i < gameObjects.size; i++) {
            GameObject gameObject = gameObjects.get(i);
            FilteredTree.Node newNode = new FilteredTree.Node(gameObject.getName(), new Label(gameObject.getName(), TalosMain.Instance().getSkin()));
            node.add(newNode);

            if(gameObject.getGameObjects() != null) {
                traverseEntityContainer(gameObject, newNode);
            }
        }

    }
}
