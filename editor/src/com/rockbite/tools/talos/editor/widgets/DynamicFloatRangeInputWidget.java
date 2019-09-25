package com.rockbite.tools.talos.editor.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class DynamicFloatRangeInputWidget extends Table {

    private CurveWidget curveWidget;

    private FloatRangeInputWidget lowInput;
    private FloatRangeInputWidget highInput;

    public DynamicFloatRangeInputWidget(Skin skin) {
       setSkin(skin);

        Table container = new Table();

        highInput = new FloatRangeInputWidget("HMin", "HMax", getSkin());
        lowInput = new FloatRangeInputWidget("LMin", "LMax", getSkin());

        lowInput.setValue(0, 0);
        highInput.setValue(1, 1);

        container.add(highInput).row();
        container.add().height(3).row();
        container.add(lowInput);

        add(container).left().expandX();

        curveWidget = new CurveWidget(getSkin());
        add(curveWidget).left().growY().width(100).padTop(23).padRight(3).padLeft(4).padBottom(3);
    }

    public void setFlavour(NumericalValue.Flavour flavour) {
        lowInput.setFlavour(flavour);
        highInput.setFlavour(flavour);
    }
}
