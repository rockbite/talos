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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.TimeUtils;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.LabelWithZoom;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.TextFieldWithZoom;

public class EditableLabel extends Table implements ActorCloneable {

    private Table labelTable;
    private Table inputTable;

    private Label label;
    private TextField textField;

    private EditableLabelChangeListener listener;

    private Vector2 tmpVec = new Vector2();

    private boolean editMode = false;
    private boolean editable = true;

    private Cell<Label> labelCell;
    private Actor keyboardFocus;

    public void setEditable (boolean editable) {
        this.editable = editable;
    }

    public interface EditableLabelChangeListener {
        void editModeStarted ();
        void changed(String newText);
    }



    public EditableLabel(String text, Skin skin) {
        super(skin);

        Stack stack = new Stack();

        labelTable = new Table();
        inputTable = new Table();

        stack.add(labelTable);
        stack.add(inputTable);

        add(stack).expand().grow();

        label = new LabelWithZoom(text, getSkin(), "default");
        label.setEllipsis(true);
        labelCell = labelTable.add(label).width(0).growX();

		TextField.TextFieldStyle textFieldStyle = getSkin().get("no-bg", TextField.TextFieldStyle.class);
		TextField.TextFieldStyle style = new TextField.TextFieldStyle(textFieldStyle);
		textField = new TextFieldWithZoom(text, style);
        inputTable.add(textField).width(0).growX();

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
                if(SceneEditorWorkspace.isEnterPressed(keycode)) {
                    finishTextEdit();
                }

                return super.keyDown(event, keycode);
            }
        });

        pack();

        setStaticMode();
    }

    public void finishTextEdit () {
        finishTextEdit(false);
    }
    public void finishTextEdit (boolean skipFocusChanges) {
        setStaticMode(skipFocusChanges);
        if(listener != null) {
            listener.changed(label.getText().toString());
        }
    }

    public void setListener(EditableLabelChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void setStage(Stage stage) {
        super.setStage(stage);
    }

    public void setEditMode() {
        if (getStage() != null) {
            keyboardFocus = getStage().getKeyboardFocus();
            getStage().setKeyboardFocus(textField);
        }

        editMode = true;
        labelTable.setVisible(false);
        inputTable.setVisible(true);

        textField.setText(label.getText().toString());

        textField.selectAll();
    }

    public void setStaticMode () {
        setStaticMode(false);
    }

    public void setStaticMode(boolean skipFocusChanges) {
        editMode = false;
        labelTable.setVisible(true);
        inputTable.setVisible(false);

        label.setText(textField.getText());
        textField.clearSelection();

        if (!skipFocusChanges) {
            if (getStage() != null) {
                if (getStage().getKeyboardFocus() == textField) {
                    getStage().setKeyboardFocus(keyboardFocus);
                }
            }
        }
    }

    @Override
    public void setColor (Color color) {
        super.setColor(color);
        textField.setColor(color);
        label.setColor(color);
		textField.getStyle().fontColor.set(color);
    }

    @Override
    public void setColor (float r, float g, float b, float a) {
        super.setColor(r, g, b, a);
        textField.setColor(r, g, b, a);
        label.setColor(r, g, b, a);
		textField.getStyle().fontColor.set(r, g, b, a);

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
        Label label = new LabelWithZoom(this.label.getText(), getSkin());
        return label;
    }
    public Label getLabel() {
        return label;
    }

    public TextField getTextField() { return textField; }

    public Cell<Label> getLabelCell() {
        return labelCell;
    }

    @Override
    public float getPrefWidth () {
        return Math.max(getTextField().getPrefWidth(), getLabelCell().getPrefWidth());
    }
}
