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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.TimeUtils;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.nodes.widgets.ValueWidget;

public class EditableLabel extends Table implements ActorCloneable {

    private Table labelTable;
    private Table inputTable;

    private Label label;
    private TextField textField;

    private EditableLabelChangeListener listener;

    private Vector2 tmpVec = new Vector2();

    private final InputListener stageListener;
    private boolean editMode = false;
    private boolean editable = true;

    private Cell<Label> labelCell;

    public void setEditable (boolean editable) {
        this.editable = editable;
    }

    public interface EditableLabelChangeListener {
        public void changed(String newText);
    }

    public EditableLabel(String text, Skin skin) {
        super(skin);

        Stack stack = new Stack();

        labelTable = new Table();
        inputTable = new Table();

        stack.add(labelTable);
        stack.add(inputTable);

        add(stack).expand().grow();

        label = new Label(text, getSkin(), "default");
        label.setEllipsis(true);
        labelCell = labelTable.add(label).growX();

        textField = new TextField(text, getSkin(), "no-bg");
        inputTable.add(textField).growX();

        addListener(new ClickListener() {

            long clickTime = 0;

            @Override
            public void clicked(InputEvent event, float x, float y) {
                long time = TimeUtils.millis();

                if(time - clickTime < 200 && editable) {
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

        stageListener = new InputListener() {
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                tmpVec.set(x, y);
                EditableLabel.this.stageToLocalCoordinates(tmpVec);
                Actor touchTarget = EditableLabel.this.hit(tmpVec.x, tmpVec.y, false);
                if (touchTarget == null && getStage() != null) {
                    getStage().setKeyboardFocus(null);
                }

                return false;
            }
        };

        pack();

        setStaticMode();
    }

    public void setListener(EditableLabelChangeListener listener) {
        this.listener = listener;
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        if (stage != null) {
            getStage().getRoot().addCaptureListener(stageListener);
        }
    }

    public void setEditMode() {
        editMode = true;
        labelTable.setVisible(false);
        inputTable.setVisible(true);
        //textField.setWidth(label.getPrefWidth() + 10);
        textField.setText(label.getText().toString());
        if( TalosMain.Instance() != null) {
            TalosMain.Instance().NodeStage().getStage().unfocusAll();
        }
        getStage().setKeyboardFocus(textField);
        textField.selectAll();
    }

    public void setStaticMode() {
        editMode = false;
        labelTable.setVisible(true);
        inputTable.setVisible(false);

        label.setText(textField.getText());
        textField.clearSelection();
    }

    @Override
    public void setColor (Color color) {
        label.setColor(color);
    }

    public String getText() {
        return label.getText().toString();
    }

    public void setText(String text) {
        label.setText(text);
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setAlignment (int alignment) {
        label.setAlignment(alignment);
        textField.setAlignment(alignment);
    }

    @Override
    public Actor copyActor (Actor copyFrom) {
        Label label = new Label(this.label.getText(), getSkin());
        return label;
    }
    public Label getLabel() {
        return label;
    }

    public Cell<Label> getLabelCell() {
        return labelCell;
    }
}
