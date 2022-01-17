package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.nodes.widgets.ValueWidget;

import java.util.function.Supplier;

public class FloatPropertyWidget extends PropertyWidget<Float>  {

    private ValueWidget valueWidget;
    private Label title;
    private ValueProperty annotation;

    public FloatPropertyWidget() {
        super();
    }

    public FloatPropertyWidget(String name, Supplier<Float> supplier, ValueChanged<Float> valueChanged) {
        super(name, supplier, valueChanged);
    }

    @Override
    protected void build (String name) {
        listener = new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                try {
                    if(event.getTarget() == valueWidget) {
                        callValueChanged(valueWidget.getValue());
                    }
                } catch (NumberFormatException e){
                    callValueChanged(0f);
                }
            }
        };

        valueWidget = new ValueWidget();
        valueWidget.init(TalosMain.Instance().getSkin());
        valueWidget.setRange(-9999, 9999);
        valueWidget.setStep(0.1f);
        valueWidget.setValue(0);
        valueWidget.setLabel("");

        title = new Label(name, TalosMain.Instance().getSkin());
        title.setAlignment(Align.left);

        add(title).minWidth(70);

        add(valueWidget).growX().maxWidth(200).right().expand();

        valueWidget.addListener(listener);
    }

    @Override
    public void updateWidget(Float value) {
        valueWidget.removeListener(listener);
        if(value == null) {
            valueWidget.setNone();
        } else {
            valueWidget.setValue(value);
        }
        valueWidget.addListener(listener);
    }

    public void setValue(float value) {
        valueWidget.setValue(value);
        this.value = value;
    }

    public void configureFromAnnotation (ValueProperty annotation) {
        if(annotation == null) return;
        valueWidget.setRange(annotation.min(), annotation.max());
        valueWidget.setStep(annotation.step());
        valueWidget.setLabel(annotation.prefix()[0]);
        valueWidget.setShowProgress(annotation.progress());

        this.annotation = annotation;
    }


    @Override
    public PropertyWidget clone() {
        FloatPropertyWidget clone = (FloatPropertyWidget) super.clone();
        clone.configureFromAnnotation(this.annotation);
        clone.title.setText(this.title.getText());

        return clone;
    }
}
