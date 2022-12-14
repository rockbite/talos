package com.talosvfx.talos.editor.addons.scene.widgets;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Predicate;
import com.kotcrab.vis.ui.util.ActorUtils;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;
import com.talosvfx.talos.editor.widgets.ui.SearchFilteredTree;

public class GameObjectListPopup extends VisWindow {

    private InputListener stageListener;
    FilteredTree<GameObject> tree;
    SearchFilteredTree<GameObject> searchFilteredTree;

    private FilteredTree.Node<GameObject> rootNode;

    public void resetSelection () {
        tree.getSelection().clear();
    }

    private FilteredTree.ItemListener<GameObject> filterTreeListener;

    private TemplateListPopup.ListListener listListener;

    public void setListener (TemplateListPopup.ListListener listener) {
        listListener = listener;
    }

    public GameObjectListPopup () {
        super("Choose Game Object", "module-list");

        setModal(false);
        setMovable(false);
        setKeepWithinParent(false);
        setKeepWithinStage(false);

        padTop(42);
        padBottom(16);
        padLeft(16);
        padRight(16);

        tree = new FilteredTree<>(getSkin());
        searchFilteredTree = new SearchFilteredTree<>(getSkin(), tree, null);

        add(searchFilteredTree).width(300).height(200).row();
        invalidate();
        pack();

        createListeners();
    }

    private void loadTree (GameObject root, Predicate<FilteredTree.Node<GameObject>> predicate) {
        tree.clearChildren();
        rootNode = new FilteredTree.Node<>(root.getName(), new Label(root.getName(), SharedResources.skin));
        rootNode.setObject(root);
        tree.add(rootNode);

        traversePath(root, rootNode, predicate);

        rootNode.setExpanded(true);
    }

    public boolean contains (float x, float y) {
        return getX() < x && getX() + getWidth() > x && getY() < y && getY() + getHeight() > y;
    }

    @Override
    protected void setStage (Stage stage) {
        super.setStage(stage);
        if (stage != null) stage.addListener(stageListener);
    }

    private void createListeners () {
        stageListener = new InputListener() {
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                if (!GameObjectListPopup.this.contains(x, y) && button == 0) {
                    remove();
                    return false;
                }
                return false;
            }
        };

        tree.addItemListener(new FilteredTree.ItemListener() {
            @Override
            public void selected (FilteredTree.Node node) {
                if (node.children.size == 0) {
                    // do shit
                }
            }

            @Override
            public void addedIntoSelection (FilteredTree.Node node) {
                super.addedIntoSelection(node);
            }
        });
    }

    public void showPopup (Stage stage, GameObject rootGameObject, Vector2 location, Predicate<FilteredTree.Node<GameObject>> filter, FilteredTree.ItemListener<GameObject> listener) {
        loadTree(rootGameObject, filter);

        setPosition(location.x, location.y - getHeight());
        if (stage.getHeight() - getY() > stage.getHeight()) setY(getY() + getHeight());
        ActorUtils.keepWithinStage(stage, this);
        stage.addActor(this);

        searchFilteredTree.reset();
        getStage().setKeyboardFocus(searchFilteredTree.textField);
        getStage().setScrollFocus(searchFilteredTree.scrollPane);
        tree.collapseAll();

        if (getHeight() < 200) {
            setHeight(200);
        }

        tree.expandAll();

        tree.addItemListener(listener);
        tree.clearSelection(false);
    }

    @Override
    public boolean remove () {
        if (getStage() != null) getStage().removeListener(stageListener);
        tree.removeItemListener(filterTreeListener);
        return super.remove();
    }

    private void traversePath (GameObject root, FilteredTree.Node<GameObject> node, Predicate<FilteredTree.Node<GameObject>> predicate) {
        Array<GameObject> gameObjects = root.getGameObjects();

        if (gameObjects == null) {
            return;
        }

        for (int i = 0; i < gameObjects.size; i++) {
            GameObject gameObject = gameObjects.get(i);

            final FilteredTree.Node<GameObject> newNode = new FilteredTree.Node<>(gameObject.getName(), new Label(gameObject.getName(), SharedResources.skin));
            newNode.setObject(gameObject);

            if (predicate.evaluate(newNode)) {
                node.add(newNode);
            }

            traversePath(gameObject, newNode, predicate);
        }
    }
}
