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
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.TimeUtils;
import com.talosvfx.talos.TalosMain;

public class EditableLabel extends Table {

    Table labelTabel;
    Table inputTable;

    Label label;
    TextField textField;

    EditableLabelChangeListener listener;

    public interface EditableLabelChangeListener {
        public void changed(String newText);
    }

    public EditableLabel(String text, Skin skin) {
        super(skin);

        Stack stack = new Stack();

        labelTabel = new Table();
        inputTable = new Table();

        stack.add(labelTabel);
        stack.add(inputTable);

        add(stack).grow();

        label = new Label(text, getSkin(), "default");
        labelTabel.add(label);
        labelTabel.add().expandX();


        textField = new TextField(text, getSkin(), "no-bg");
        inputTable.add(textField);
        inputTable.add().expandX();

        addListener(new ClickListener() {

            long clickTime = 0;

            @Override
            public void clicked(InputEvent event, float x, float y) {
                long time = TimeUtils.millis();

                if(time - clickTime < 200) {
                    setEditMode();
                }

                clickTime = time;

                super.clicked(event, x, y);
            }
        });

        textField.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if(keycode == Input.Keys.ENTER) {
                    setStaticMode();
                    if(listener != null) {
                        listener.changed(label.getText().toString());
                    }
                }

                return super.keyDown(event, keycode);
            }
        });

        textField.addListener(new FocusListener() {
            @Override
            public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
                super.keyboardFocusChanged(event, actor, focused);
                if(!focused) {
                    setStaticMode();
                    if(listener != null) {
                        listener.changed(label.getText().toString());
                    }
                }
            }
        });

        pack();

        setStaticMode();
    }

    public void setListener(EditableLabelChangeListener listener) {
        this.listener = listener;
    }

    public void setEditMode() {
        labelTabel.setVisible(false);
        inputTable.setVisible(true);
        textField.setWidth(label.getPrefWidth());
        textField.setText(label.getText().toString());
        TalosMain.Instance().NodeStage().getStage().unfocusAll();
        getStage().setKeyboardFocus(textField);
        textField.selectAll();
    }

    public void setStaticMode() {
        labelTabel.setVisible(true);
        inputTable.setVisible(false);

        label.setText(textField.getText());
        textField.clearSelection();
    }

    public String getText() {
        return label.getText().toString();
    }

    public void setText(String text) {
        label.setText(text);
    }
}
