package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.TalosMain;


public abstract class SelectBoxWidget extends PropertyWidget<String> {

    Stack stack;
    Label noValueLabel;
    SelectBox<String> selectBox;

    public SelectBoxWidget(String name) {
        super(name);
    }

    @Override
    public Actor getSubWidget() {
        selectBox = new SelectBox<>(TalosMain.Instance().UIStage().getSkin(), "propertyValue");
        noValueLabel = new Label("", TalosMain.Instance().UIStage().getSkin());
        noValueLabel.setAlignment(Align.right);
        stack = new Stack();
        stack.add(noValueLabel);
        stack.add(selectBox);

        noValueLabel.setVisible(false);

        listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String newValue = selectBox.getSelected();
                valueChanged(newValue);
            }
        };
        selectBox.addListener(listener);

        return stack;
    }

    @Override
    public void updateWidget(String value) {
        Array<String> list = getOptionsList();
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

    public abstract Array<String> getOptionsList();
}
