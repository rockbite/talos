package com.talosvfx.talos.editor.widgets.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class NumericalValueField extends Table {

    private final TextField x;
    private final TextField y;
    private final TextField z;
    NumericalValue value;

    public NumericalValueField(Skin skin) {
        setSkin(skin);
        x = new TextField("0.0", getSkin(), "panel");
        x.setTextFieldFilter(new FloatTextFilter());
        y = new TextField("0.0", getSkin(), "panel");
        y.setTextFieldFilter(new FloatTextFilter());
        z = new TextField("0.0", getSkin(), "panel");
        z.setTextFieldFilter(new FloatTextFilter());

        x.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String text = x.getText().isEmpty() ? "0" : x.getText();
                value.set(0, Float.parseFloat(text));
            }
        });

        y.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String text = x.getText().isEmpty() ? "0" : y.getText();
                value.set(1, Float.parseFloat(text));
            }
        });

        z.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String text = x.getText().isEmpty() ? "0" : z.getText();
                value.set(2, Float.parseFloat(text));
            }
        });

        add(x).padRight(6f).prefWidth(52).minWidth(10).growX().height(25);
        add(y).prefWidth(52).minWidth(10).growX().height(25);
        add(z).padLeft(6f).prefWidth(52).minWidth(10).growX().height(25);
    }

    public void setNumericalValue (NumericalValue value) {
        this.value = value;
        this.x.setText(String.valueOf(value.get(0)));
        this.y.setText(String.valueOf(value.get(1)));
        this.z.setText(String.valueOf(value.get(2)));
    }

    private static class FloatTextFilter implements TextField.TextFieldFilter {
        @Override
        public boolean acceptChar (TextField textField, char c) {
            return Character.isDigit(c) || (c == '.' && !textField.getText().contains("."));
        }
    }
}
