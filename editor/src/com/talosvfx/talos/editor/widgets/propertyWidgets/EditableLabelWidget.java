package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Align;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.EditableLabel;

import com.talosvfx.talos.runtime.utils.Supplier;

public class EditableLabelWidget extends PropertyWidget<String> {

    private EditableLabel propertyValue;
    protected EditableLabelWidget () {}

    public EditableLabelWidget(String name, Supplier<String> supplier, ValueChanged<String> valueChanged, Object parent) {
        super(name, supplier, valueChanged, parent);
    }

    @Override
    public Actor getSubWidget() {
        propertyValue = new EditableLabel("", SharedResources.skin);
        propertyValue.setAlignment(Align.right);

        propertyValue.addListener(new FocusListener() {
            @Override
            public void keyboardFocusChanged (FocusEvent event, Actor actor, boolean focused) {
                super.keyboardFocusChanged(event, actor, focused);
                if (!focused) {
                    propertyValue.finishTextEdit(true);
                }
            }
        });

        propertyValue.setListener(new EditableLabel.EditableLabelChangeListener() {
            @Override
            public void editModeStarted () {

            }

            @Override
            public void changed (String newText) {
                callValueChanged(newText);
            }
        });

        return propertyValue;
    }

    @Override
    public void updateWidget(String value) {
        if(value == null) {
            propertyValue.setText("-");
            propertyValue.setEditable(false);
        } else {
            propertyValue.setText(value);
            propertyValue.setEditable(true);
        }
    }

    public void setText(String value) {
        propertyValue.setText(value + "");
        this.value = value;
    }

    @Override
    public PropertyWidget clone() {
        PropertyWidget clone = super.clone();
        return clone;
    }
}
