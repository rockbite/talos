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
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;
import com.talosvfx.talos.editor.widgets.ui.SearchFilteredTree;

public class GenericListPopup<T> extends VisWindow {

    private InputListener stageListener;

    private FilteredTree<T> tree;
    private SearchFilteredTree<T> searchFilteredTree;

    private TemplateListPopup.ListListener listListener;

    public GenericListPopup(String title) {
        super(title, "module-list");

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

        build();

        invalidate();
        pack();

        createListeners();
    }

    protected void build () {
        add(searchFilteredTree).width(300).height(200).row();
    }

    @Override
    protected void setStage (Stage stage) {
        super.setStage(stage);
        if (stage != null) stage.addListener(stageListener);
    }

    @Override
    public boolean remove () {
        if (getStage() != null) getStage().removeListener(stageListener);
        return super.remove();
    }

    public void showPopup (Stage stage, Array<T> items, Vector2 location, Predicate<FilteredTree.Node<T>> filter, FilteredTree.ItemListener<T> listener) {
        loadTree(items, filter);

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

    private void loadTree (Array<T> items, Predicate<FilteredTree.Node<T>> predicate) {
        tree.clearChildren();

        for (T item : items) {
            final FilteredTree.Node<T> newNode = new FilteredTree.Node<>(item.toString(), new Label(item.toString(), SharedResources.skin));
            newNode.setObject(item);

            if (predicate.evaluate(newNode)) {
                tree.add(newNode);
            }
        }
    }


    private boolean contains (float x, float y) {
        return getX() < x && getX() + getWidth() > x && getY() < y && getY() + getHeight() > y;
    }

    private void createListeners () {
        stageListener = new InputListener() {
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                if (!GenericListPopup.this.contains(x, y) && button == 0) {
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
}
