package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.project2.SharedResources;

import java.util.function.Supplier;


public class SelectBoxWidget extends PropertyWidget<String> {

    Stack stack;
    Label noValueLabel;
    SelectBox<String> selectBox;

    Supplier<Array<String>> optionListSupplier;

    public SelectBoxWidget() {
        super();
    }

    public SelectBoxWidget(String name, Supplier<String> supplier, ValueChanged<String> valueChanged, Supplier<Array<String>> optionListSupplier) {
        super(name, supplier, valueChanged);
        setOptionListSupplier(optionListSupplier);
    }

    @Override
    public PropertyWidget clone() {
        SelectBoxWidget clone = (SelectBoxWidget) super.clone();
        clone.setOptionListSupplier(this.optionListSupplier);

        return clone;
    }

    @Override
    public Actor getSubWidget() {
        selectBox = new SelectBox<>(SharedResources.skin, "propertyValue");
        noValueLabel = new Label("", SharedResources.skin);
        noValueLabel.setAlignment(Align.right);
        stack = new Stack();
        stack.add(noValueLabel);
        stack.add(selectBox);

        noValueLabel.setVisible(false);

        listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String newValue = selectBox.getSelected();
                callValueChanged(newValue);
            }
        };
        selectBox.addListener(listener);

        return stack;
    }

    @Override
    public void updateWidget(String value) {
        Array<String> list = optionListSupplier.get();
        if(list != null) {
            selectBox.removeListener(listener);
            selectBox.setItems(list);
            selectBox.setVisible(true);
            noValueLabel.setVisible(false);
            selectBox.setSelected(value);
            selectBox.addListener(listener);
        } else {
            // show label with N/A
            selectBox.setVisible(false);
            noValueLabel.setVisible(true);
            noValueLabel.setText("N/A");
        }
    }

    public void setOptionListSupplier(Supplier<Array<String>> supplier) {
        this.optionListSupplier = supplier;
    }
}
