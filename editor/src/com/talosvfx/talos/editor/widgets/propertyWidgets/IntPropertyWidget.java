package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.talosvfx.talos.TalosMain;

public abstract class IntPropertyWidget extends PropertyWidget<Integer> {

    private TextField textField;

    public IntPropertyWidget(String name) {
        super(name);
    }

    @Override
    public Actor getSubWidget() {
        textField = new TextField("", TalosMain.Instance().getSkin(), "panel");
        textField.setTextFieldFilter(new IntFieldFilter());

        listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(textField.getText().isEmpty()) return;
                try {
                    callValueChanged(Integer.parseInt(textField.getText()));
                } catch (NumberFormatException e){
                    callValueChanged(0);
                }
            }
        };
        textField.addListener(listener);
        return textField;
    }

    @Override
    public void updateWidget(Integer value) {
        textField.removeListener(listener);
        textField.setText(value + "");
        textField.addListener(listener);
    }

    public void setValue(int value) {
        textField.setText(value + "");
        this.value = value;
    }
}