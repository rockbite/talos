package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.talosvfx.talos.TalosMain;

public abstract class Vector2PropertyWidget extends PropertyWidget<Vector2>  {

    private TextField xField;
    private TextField yField;

    public Vector2PropertyWidget(String name) {
        super(name);
    }

    @Override
    public Actor getSubWidget() {
        Table table = new Table();

        xField = new TextField("", TalosMain.Instance().getSkin(), "panel");
        xField.setTextFieldFilter(new FloatFieldFilter());

        yField = new TextField("", TalosMain.Instance().getSkin(), "panel");
        yField.setTextFieldFilter(new FloatFieldFilter());

        listener = new ChangeListener() {

            Vector2 vec = new Vector2();

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(xField.getText().isEmpty()) return;
                if(yField.getText().isEmpty()) return;
                try {
                    float x = Float.parseFloat(xField.getText());
                    float y = Float.parseFloat(yField.getText());
                    vec.set(x, y);
                    valueChanged(vec);
                } catch (NumberFormatException e){
                    vec.set(0, 0);
                    valueChanged(vec);
                }
            }
        };
        xField.addListener(listener);
        yField.addListener(listener);

        table.add(new Label("x: ", TalosMain.Instance().getSkin())).padRight(4);
        table.add(xField).width(63);
        table.add(new Label("y: ", TalosMain.Instance().getSkin())).padRight(4).padLeft(4);
        table.add(yField).width(63);

        return table;
    }

    @Override
    public void updateWidget(Vector2 value) {
        xField.removeListener(listener);
        yField.removeListener(listener);

        xField.setText(value.x + "");
        yField.setText(value.y + "");

        xField.addListener(listener);
        yField.addListener(listener);
    }

    public void setValue(Vector2 value) {

        xField.setText(value.x + "");
        yField.setText(value.y + "");

        this.value = value;
    }
}
