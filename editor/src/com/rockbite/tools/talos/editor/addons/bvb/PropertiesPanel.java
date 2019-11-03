package com.rockbite.tools.talos.editor.addons.bvb;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.data.PropertyProviderCenter;
import com.rockbite.tools.talos.editor.widgets.propertyWidgets.GlobalValueWidget;
import com.rockbite.tools.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.rockbite.tools.talos.editor.wrappers.IPropertyProvider;
import com.rockbite.tools.talos.editor.wrappers.MutableProperty;
import com.rockbite.tools.talos.editor.wrappers.Property;

public class PropertiesPanel extends Window {

    Table propertyGroup = new Table();
    Array<IPropertyProvider> currentPropertyPanels = new Array<>();

    Array<PropertyWidget> propertyWidgets = new Array<>();

    public PropertiesPanel(Skin skin) {
        super("Global Properties", skin);
        setBackground(skin.getDrawable("panel"));

        padLeft(5);
        padTop(25);

        setModal(false);
        setMovable(false);
        ScrollPane scrollPane = new ScrollPane(propertyGroup);
        scrollPane.setScrollingDisabled(true, false);
        add(scrollPane).grow();

        reconstruct();
    }

    @Override
    public void act (float delta) {
        super.act(delta);
        for (PropertyWidget propertyWidget : propertyWidgets) {
            propertyWidget.refresh();
        }
    }

    public void addProperty (IPropertyProvider propertyProvider) {
        currentPropertyPanels.clear();
        currentPropertyPanels.add(propertyProvider);
        reconstruct();
    }

    private void reconstruct () {
        propertyGroup.clear();
        propertyWidgets.clear();
        propertyGroup.top().left();

        Table propertyTable = new Table();
        propertyGroup.add(propertyTable).growX();
        for (IPropertyProvider currentPropertyPanel : currentPropertyPanels) {
            Array<Property> listOfProperties = currentPropertyPanel.getListOfProperties();
            for (Property property : listOfProperties) {
                PropertyWidget propertyWidget = PropertyProviderCenter.Instance().obtainWidgetForProperty(property);
                propertyWidgets.add(propertyWidget);
                propertyTable.add(propertyWidget).growX();
                propertyTable.row();
            }
        }
    }




}
