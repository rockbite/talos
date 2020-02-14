package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.talosvfx.talos.TalosMain;

public abstract class FloatPropertyWidget extends PropertyWidget<Float>  {

    private TextField textField;

    public FloatPropertyWidget(String name) {
        super(name);
    }

    @Override
    public Actor getSubWidget() {
        textField = new TextField("", TalosMain.Instance().getSkin(), "panel");
        textField.setTextFieldFilter(new FloatFieldFilter());

        listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(textField.getText().isEmpty()) return;
                try {
                    valueChanged(Float.parseFloat(textField.getText()));
                } catch (NumberFormatException e){
                    valueChanged(0f);
                }
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

    public void setValue(float value) {
        textField.setText(value + "");
        this.value = value;
    }
}
