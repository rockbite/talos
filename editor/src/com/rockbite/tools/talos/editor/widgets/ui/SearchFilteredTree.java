package com.rockbite.tools.talos.editor.widgets.ui;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class SearchFilteredTree<T> extends Table {

    public TextField textField;
    private FilteredTree<T> filteredTree;
    public ScrollPane scrollPane;

    public SearchFilteredTree (Skin skin, final FilteredTree<T> tree, TextField.TextFieldFilter filter) {

        Table searchTable = new Table();
        Image image = new Image(skin.newDrawable("search"));

        textField = new TextField("", skin);
        if (filter != null) {
            textField.setTextFieldFilter(filter);
        }

        searchTable.add(image);
        searchTable.add(textField).growX();

        filteredTree = tree;

        add(searchTable).growX();
        row();
        scrollPane = new ScrollPane(filteredTree, skin, "list");
        add(scrollPane).growX();

        textField.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                tree.filter(textField.getText());
                tree.invalidateHierarchy();
                tree.invalidate();
                tree.layout();
            }
        });

    }

    public void reset() {
        textField.setText("");
        filteredTree.filter(textField.getText());
        filteredTree.invalidateHierarchy();
        filteredTree.invalidate();
        filteredTree.layout();
    }

}
