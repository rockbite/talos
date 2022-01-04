package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Pool;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.nodes.widgets.ValueWidget;

import java.util.function.Supplier;

public class Vector2PropertyWidget extends PropertyWidget<Vector2>  {

    private ValueWidget xValue;
    private ValueWidget yValue;

    public Vector2PropertyWidget(String name, Supplier<Vector2> supplier, ValueChanged<Vector2> valueChanged) {
        super(name, supplier, valueChanged);
    }

    @Override
    protected void build (String name) {
        listener = new ChangeListener() {

            Vector2 vec = new Vector2();

            @Override
            public void changed (ChangeEvent event, Actor actor) {
                try {
                    vec.set(xValue.getValue(), yValue.getValue());
                    callValueChanged(vec);
                } catch (NumberFormatException e){
                    vec.set(0, 0);
                    callValueChanged(vec);
                }
            }
        };

        xValue = new ValueWidget();
        xValue.init(TalosMain.Instance().getSkin());
        xValue.setRange(-9999, 9999);
        xValue.setStep(0.1f);
        xValue.setValue(0);
        xValue.setLabel("X");
        xValue.setType(ValueWidget.Type.TOP);
        xValue.addListener(listener);

        yValue = new ValueWidget();
        yValue.init(TalosMain.Instance().getSkin());
        yValue.setRange(-9999, 9999);
        yValue.setStep(0.1f);
        yValue.setValue(0);
        yValue.setLabel("Y");
        yValue.setType(ValueWidget.Type.BOTTOM);
        yValue.addListener(listener);

        Table left = new Table();
        Table right = new Table();

        Label title = new Label(name, TalosMain.Instance().getSkin());
        title.setAlignment(Align.left);

        left.add(title).left().expand().pad(2).top();
        left.row();
        left.add().growY().expand();
        right.add(xValue).growX().maxWidth(250).right().expand();
        right.row();
        right.add(yValue).growX().padTop(1).maxWidth(250).right().expand();

        add(left).growY().minWidth(70);
        add(right).growX();
    }

    @Override
    public void updateWidget(Vector2 value) {
        xValue.removeListener(listener);
        yValue.removeListener(listener);

        xValue.setValue(value.x);
        yValue.setValue(value.y);

        xValue.addListener(listener);
        yValue.addListener(listener);
    }

    public void setValue(Vector2 value) {

        xValue.setValue(value.x);
        yValue.setValue(value.y);

        this.value = value;
    }

    public void configureFromAnnotation (ValueProperty annotation) {
        if(annotation == null) return;
        xValue.setRange(annotation.min(), annotation.max());
        xValue.setStep(annotation.step());
        xValue.setLabel(annotation.prefix()[0]);
        xValue.setShowProgress(annotation.progress());

        yValue.setRange(annotation.min(), annotation.max());
        yValue.setStep(annotation.step());
        yValue.setLabel(annotation.prefix()[1]);
        yValue.setShowProgress(annotation.progress());
    }
}
