package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorProject;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.GameObjectContainer;
import com.talosvfx.talos.editor.project.IProject;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;

public class HierarchyWidget extends Table {

    private FilteredTree tree;

    private ObjectMap<String, GameObject> objectMap = new ObjectMap<>();

    public HierarchyWidget() {
        tree = new FilteredTree<>(TalosMain.Instance().getSkin(), "modern");

        add(tree).grow().pad(5).padRight(0);

        tree.setItemListener(new FilteredTree.ItemListener() {
            @Override
            public void chosen (FilteredTree.Node node) {
                SceneEditorAddon sceneEditorAddon = SceneEditorAddon.get();
                sceneEditorAddon.workspace.selectPropertyHolder(objectMap.get(node.getName()));

            }

            @Override
            public void selected (FilteredTree.Node node) {

            }
        });
    }

    public void loadEntityContainer(GameObjectContainer entityContainer) {
        tree.clearChildren();
        objectMap.clear();

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

            objectMap.put(gameObject.getName(), gameObject);

            if(gameObject.getGameObjects() != null) {
                traverseEntityContainer(gameObject, newNode);
            }
        }

    }
}
