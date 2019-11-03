package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.rockbite.tools.talos.TalosMain;


public class SelectBoxWidget extends PropertyWidget<String> {

    Stack stack;
    Label noValueLabel;
    SelectBox<String> selectBox;

    @Override
    public Actor getValueActor() {
        selectBox = new SelectBox<>(TalosMain.Instance().UIStage().getSkin(), "propertyValue");
        noValueLabel = new Label("", TalosMain.Instance().UIStage().getSkin());
        noValueLabel.setAlignment(Align.right);
        stack = new Stack();
        stack.add(noValueLabel);
        stack.add(selectBox);

        noValueLabel.setVisible(false);

        return stack;
    }

    @Override
    public void configureForProperty(Property property) {
        super.configureForProperty(property);

        refresh();
    }

    @Override
    public void refresh() {
        StringListProperty stringListProperty = (StringListProperty) bondedProperty;
        Array<String> list = stringListProperty.getOptionsList();
        if(list != null) {
            selectBox.setItems(list);
            selectBox.setVisible(true);
            noValueLabel.setVisible(false);
        } else {
            // show label with N/A
            selectBox.setVisible(false);
            noValueLabel.setVisible(true);
            noValueLabel.setText("N/A");
        }
    }
}
