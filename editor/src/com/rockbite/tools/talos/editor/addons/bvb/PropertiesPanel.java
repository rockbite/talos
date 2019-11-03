package com.rockbite.tools.talos.editor.addons.bvb;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.rockbite.tools.talos.editor.widgets.propertyWidgets.PropertyProviderCenter;
import com.rockbite.tools.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.rockbite.tools.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.rockbite.tools.talos.editor.widgets.propertyWidgets.Property;

public class PropertiesPanel extends Window {

    Table propertyGroup = new Table();
    Array<IPropertyProvider> currentPropertyPanels = new Array<>();

    Array<PropertyWidget> propertyWidgets = new Array<>();

    public PropertiesPanel(IPropertyProvider propertyProvider, Skin skin) {
        super(propertyProvider.getPropertyBoxTitle(), skin);
        setBackground(skin.getDrawable("panel"));

        padLeft(5);
        padTop(25);

        setModal(false);
        setMovable(false);
        ScrollPane scrollPane = new ScrollPane(propertyGroup);
        scrollPane.setScrollingDisabled(true, false);
        add(scrollPane).grow();

        setPropertyProvider(propertyProvider);
    }

    @Override
    public void act (float delta) {
        super.act(delta);
        for (PropertyWidget propertyWidget : propertyWidgets) {
            propertyWidget.refresh();
        }
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
            Array<Property> listOfProperties = currentPropertyPanel.getListOfProperties();
            if(listOfProperties != null) {
                for (Property property : listOfProperties) {
                    PropertyWidget propertyWidget = PropertyProviderCenter.Instance().obtainWidgetForProperty(property);
                    propertyWidgets.add(propertyWidget);
                    propertyTable.add(propertyWidget).growX().pad(5f);
                    propertyTable.row();
                }
            }
        }
    }




}
