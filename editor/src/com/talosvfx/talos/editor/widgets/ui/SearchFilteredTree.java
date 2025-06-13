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


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.project2.SharedResources;

public class SearchFilteredTree<T> extends Table {
    private static final float AUTO_SCROLL_RANGE = 40;
    private static final float AUTO_SCROLL_SPEED = 500;

    public TextField textField;
    private FilteredTree<T> filteredTree;
    public ScrollPane scrollPane;

    private boolean autoSelect = true;

    private Table searchTable;

    private Vector2 tmp = new Vector2();
    public SearchFilteredTree (Skin skin, final FilteredTree<T> tree, final TextField.TextFieldFilter filter) {

        searchTable = new Table();
        Image image = new Image(skin.newDrawable("search"));

        textField = new TextField("", skin);
        if (filter != null) {
            textField.setTextFieldFilter(filter);
        }

        ImageButton collapseAllButton = new ImageButton(SharedResources.skin, "collapse-all");
        ImageButton expandAllButton = new ImageButton(SharedResources.skin, "expand-all");

        collapseAllButton.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                tree.collapseAll();
            }
        });

        expandAllButton.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                tree.expandAll();
            }
        });

        searchTable.padRight(5);
        searchTable.add(image);
        searchTable.add(textField).growX().spaceLeft(5);
        searchTable.add(collapseAllButton).spaceLeft(5).height(20);
        searchTable.add(expandAllButton).spaceLeft(5).height(20);

        filteredTree = tree;
        filteredTree.setSearchFilteredTree(this);

        add(searchTable).growX();
        row();
        scrollPane = new ScrollPane(filteredTree, skin, "list");
        add(scrollPane).grow();

        textField.addListener(new ChangeListener() {
            boolean wasEmpty = true;
            FilteredTree.TreeState savedState;

            @Override
            public void changed (ChangeEvent event, Actor actor) {
                String typedText = textField.getText();

                if (wasEmpty && !typedText.isEmpty()) {
                    // it was empty before, and we just wrote something, save this state
                    savedState = filteredTree.getCurrentState();
                }

                tree.smartFilter(typedText, autoSelect);

                if (!wasEmpty && typedText.isEmpty()) {
                    // we just made removed all text and left it empty, try to restore the state
                    if (savedState != null) {
                        filteredTree.restoreFromState(savedState);
                    }
                }

                tree.invalidateHierarchy();
                tree.invalidate();
                tree.layout();

                wasEmpty = typedText.isEmpty();
            }
        });

        textField.addListener(new ClickListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode)  {
                if(SceneEditorWorkspace.isEnterPressed(keycode)) {
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

    public void setPad (float padTop, float padLeft, float padBottom, float padRight) {
        Cell<TextField> textFieldCell = searchTable.getCell(textField);
        textFieldCell.pad(padTop, padLeft, padBottom, padRight);
    }

    public void setAutoSelect(boolean autoSelect) {
        this.autoSelect = autoSelect;
    }

    public void onItemHold() {
        tmp.set(Gdx.input.getX(), Gdx.input.getY());
        scrollPane.screenToLocalCoordinates(tmp);

        if (isInTopZone(tmp.x, tmp.y)) {
            scrollPane.setScrollY(scrollPane.getScrollY() - AUTO_SCROLL_SPEED * Gdx.graphics.getDeltaTime());
        } else if (isInBottomZone(tmp.x, tmp.y)) {
            scrollPane.setScrollY(scrollPane.getScrollY() + AUTO_SCROLL_SPEED * Gdx.graphics.getDeltaTime());
        }
    }

    private boolean isInTopZone(float localX, float localY) {
        return localY < scrollPane.getHeight() && localY > scrollPane.getHeight() -  AUTO_SCROLL_RANGE;
    }

    private boolean isInBottomZone(float localX, float localY) {
        return localY > 0 && localY < AUTO_SCROLL_RANGE;
    }
}
