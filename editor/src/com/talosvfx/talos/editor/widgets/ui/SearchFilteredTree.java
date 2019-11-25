/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.talosvfx.talos.editor.widgets.ui;


import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class SearchFilteredTree<T> extends Table {

    public TextField textField;
    private FilteredTree<T> filteredTree;
    public ScrollPane scrollPane;

    public SearchFilteredTree (Skin skin, final FilteredTree<T> tree, final TextField.TextFieldFilter filter) {

        Table searchTable = new Table();
        Image image = new Image(skin.newDrawable("search"));

        textField = new TextField("", skin);
        if (filter != null) {
            textField.setTextFieldFilter(filter);
        }

        searchTable.add(image);
        searchTable.add(textField).growX().padLeft(5);

        filteredTree = tree;

        add(searchTable).growX();
        row();
        scrollPane = new ScrollPane(filteredTree, skin, "list");
        add(scrollPane).growX();

        textField.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                tree.smartFilter(textField.getText());
                tree.invalidateHierarchy();
                tree.invalidate();
                tree.layout();
            }
        });

        textField.addListener(new ClickListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode)  {
                if(keycode == Input.Keys.UP) {
                    filteredTree.selectPrevFilteredNode();
                }
                if(keycode == Input.Keys.DOWN) {
                    filteredTree.selectNextFilteredNode();
                }

                if(keycode == Input.Keys.ENTER) {
                    filteredTree.reportUserEnter();
                }
                return super.keyDown(event, keycode);
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
