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

package com.talosvfx.talos.editor.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

public class IntegerInputWidget extends Table {


    private ChangeListener listener;

    private TextField regularCarrier;
    private Label regularLabel;
    private int value;

    public IntegerInputWidget(String text, Skin skin) {
        make(text, skin, Align.left);
    }

    public IntegerInputWidget(String text, Skin skin, int align) {
        make(text, skin, align);
    }

    private void make(String text, Skin skin, int align) {
        setSkin(skin);

        Table table = new Table();

        regularLabel = new Label(text, getSkin());
        regularCarrier = new TextField("", getSkin());
        regularCarrier.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());

        Cell labelCell = table.add(regularLabel);
        if(align == Align.left) {
            labelCell.left();
        } else {
            labelCell.right();
        }
        labelCell.row();
        Cell carrierCell = table.add(regularCarrier);
        if(align == Align.left) {
            carrierCell.left();
        } else {
            carrierCell.right();
        }
        carrierCell.padTop(5).width(68);

        regularCarrier.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getValue();
                if(listener != null) {
                    listener.changed(event, actor);
                }
            }
        });

        regularCarrier.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);

                if(regularCarrier.getSelection().length() == 0) {
                    regularCarrier.selectAll();
                }
            }
        });

        add(table).padLeft(4);
    }

    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }

    public int getValue() {
        int val = 0;
        try {
            val = Integer.parseInt(regularCarrier.getText());
        } catch (NumberFormatException e) {
            val = 0;
        }
        return val;
    }

    public void setValue(int val) {
        value = val;
        regularCarrier.setText(Integer.toString(val));
    }

    public void setText(String text) {
        regularLabel.setText(text);
    }
}
