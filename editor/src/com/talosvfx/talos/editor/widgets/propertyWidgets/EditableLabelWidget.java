package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.widgets.ui.EditableLabel;

import java.util.function.Supplier;

public class EditableLabelWidget extends PropertyWidget<String> {

    private EditableLabel propertyValue;

    public EditableLabelWidget() {
        super();
    }

    public EditableLabelWidget(String name, Supplier<String> supplier, ValueChanged<String> valueChanged) {
        super(name, supplier, valueChanged);
    }

    @Override
    public Actor getSubWidget() {
        propertyValue = new EditableLabel("", TalosMain.Instance().getSkin());
        propertyValue.setAlignment(Align.right);

        propertyValue.setListener(new EditableLabel.EditableLabelChangeListener() {
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
