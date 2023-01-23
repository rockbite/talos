package com.talosvfx.talos.editor.widgets.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.talosvfx.talos.editor.widgets.propertyWidgets.FloatFieldFilter;
import com.talosvfx.talos.runtime.vfx.values.NumericalValue;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.TextFieldWithZoom;

public class NumericalValueField extends Table {

    private final TextField x;
    private final TextField y;
    private final TextField z;
    NumericalValue value;

    public NumericalValueField(Skin skin) {
        setSkin(skin);
        x = new TextFieldWithZoom("0.0", getSkin(), "panel");
        x.setTextFieldFilter(new FloatFieldFilter());
        y = new TextFieldWithZoom("0.0", getSkin(), "panel");
        y.setTextFieldFilter(new FloatFieldFilter());
        z = new TextFieldWithZoom("0.0", getSkin(), "panel");
        z.setTextFieldFilter(new FloatFieldFilter());

        x.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    float parseFloat = Float.parseFloat(x.getText());
                    value.set(0, parseFloat);
                } catch (NumberFormatException e) {
                    value.set(0, 0f);
                }
            }
        });

        y.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    float parseFloat = Float.parseFloat(y.getText());
                    value.set(1, parseFloat);
                } catch (NumberFormatException e) {
                    value.set(1, 0f);
                }
            }
        });

        z.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    float parseFloat = Float.parseFloat(z.getText());
                    value.set(2, parseFloat);
                } catch (NumberFormatException e) {
                    value.set(2, 0f);
                }
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
}
