package com.rockbite.tools.talos.editor.addons.bvb;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.rockbite.tools.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.rockbite.tools.talos.editor.widgets.propertyWidgets.IPropertyProvider;

public class PropertiesPanel extends Window {

    Table propertyGroup = new Table();
    Array<IPropertyProvider> currentPropertyPanels = new Array<>();

    Array<PropertyWidget> propertyWidgets = new Array<>();

    public PropertiesPanel(IPropertyProvider propertyProvider, Skin skin) {
        super(propertyProvider.getPropertyBoxTitle(), skin, "default-nodim");
        setBackground(skin.getDrawable("panel"));

        padLeft(5);
        padTop(25);

        setModal(false);
        setMovable(false);
        setResizable(false);
        setRound(false);

        ScrollPane scrollPane = new ScrollPane(propertyGroup);
        scrollPane.setScrollingDisabled(true, false);
        add(scrollPane).grow();

        setPropertyProvider(propertyProvider);
    }

    @Override
    public void act (float delta) {
        super.act(delta);
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
                    propertyTable.add(propertyWidget).growX().pad(5f);
                    propertyTable.row();
                }
            }
        }
    }




}
