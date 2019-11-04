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
        final TextField x = new TextField("0.0", getSkin(), "panel");
        x.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        final TextField y = new TextField("0.0", getSkin(), "panel");
        y.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        final TextField z = new TextField("0.0", getSkin(), "panel");
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

        add(x).padRight(6f).prefWidth(52).minWidth(10).growX().height(25);
        add(y).prefWidth(52).minWidth(10).growX().height(25);
        add(z).padLeft(6f).prefWidth(52).minWidth(10).growX().height(25);
    }
}