package com.rockbite.tools.talos.editor.widgets.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class NumericalValueField extends Table {

    NumericalValue value;

    public NumericalValueField(Skin skin) {
        setSkin(skin);
        final TextField x = new TextField("", getSkin());
        x.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        final TextField y = new TextField("", getSkin());
        y.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        final TextField z = new TextField("", getSkin());
        z.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());

        x.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                value.set(0, Float.parseFloat(x.getText()));
            }
        });

        y.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                value.set(0, Float.parseFloat(x.getText()));
            }
        });

        z.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                value.set(0, Float.parseFloat(x.getText()));
            }
        });

        add(x);
        add(y);
        add(z);
    }
}