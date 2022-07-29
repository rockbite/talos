package com.talosvfx.talos.editor.addons.bvb;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;

public class PropertiesPanel extends Table {

    Label titleLabel;
    Table propertyGroup = new Table();
    Array<IPropertyProvider> currentPropertyPanels = new Array<>();

    Array<PropertyWidget> propertyWidgets = new Array<>();

    public PropertiesPanel(IPropertyProvider propertyProvider, Skin skin) {
        setSkin(skin);
        setBackground(skin.getDrawable("panel"));

        padLeft(5);
        padTop(25);

        setRound(false);

        titleLabel = new Label("", getSkin());
        titleLabel.setEllipsis(true);
        Table titleContainer = new Table();
        titleContainer.add(titleLabel).padTop(-titleLabel.getPrefHeight()-3).growX().left().width(0).row();
        titleContainer.add().expandY();

        Stack stack = new Stack();
        stack.add(propertyGroup);
        stack.add(titleContainer);

        add(stack).grow();

        setPropertyProvider(propertyProvider);

        setTitle(propertyProvider.getPropertyBoxTitle());
    }

    private void setTitle(String title) {
        titleLabel.setText(title);
    }

    private void setPropertyProvider (IPropertyProvider propertyProvider) {
        currentPropertyPanels.clear();
        currentPropertyPanels.add(propertyProvider);
        reconstruct();
    }

    private void reconstruct () {
        propertyGroup.clear();
        propertyWidgets.clear();
        propertyGroup.top().left();

        Table propertyTable = new Table();
        propertyGroup.add(propertyTable).growX().padRight(5);
        for (IPropertyProvider currentPropertyPanel : currentPropertyPanels) {

            Array<PropertyWidget> listOfProperties = currentPropertyPanel.getListOfProperties();

            if(listOfProperties != null) {
                for (PropertyWidget propertyWidget : listOfProperties) {
                    propertyWidgets.add(propertyWidget);
                    propertyWidget.updateValue();
                    propertyTable.add(propertyWidget).growX().pad(5f).padBottom(0);
                    propertyTable.row();
                }
                propertyTable.add().height(5);
            }
        }
    }


    public void updateValues() {
        for (int i = 0; i < propertyWidgets.size; i++) {
            PropertyWidget widget = propertyWidgets.get(i);
            widget.updateValue();
        }
    }
}
