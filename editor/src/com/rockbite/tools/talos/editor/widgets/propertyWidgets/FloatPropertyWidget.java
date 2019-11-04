package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.rockbite.tools.talos.TalosMain;

public abstract class FloatPropertyWidget extends PropertyWidget<Float>  {

    private TextField textField;

    public FloatPropertyWidget(String name) {
        super(name);
    }

    @Override
    public Actor getSubWidget() {
        textField = new TextField("", TalosMain.Instance().getSkin(), "panel");
        textField.setTextFieldFilter(new TextField.TextFieldFilter() {
            @Override
            public boolean acceptChar(TextField textField, char c) {
                return Character.isDigit(c) || c == '.';
            }
        });

        listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(textField.getText().isEmpty()) return;

                valueChanged(Float.parseFloat(textField.getText()));
            }
        };
        textField.addListener(listener);
        return textField;
    }

    @Override
    public void updateWidget(Float value) {
        textField.removeListener(listener);
        textField.setText(value + "");
        textField.addListener(listener);
    }
}
