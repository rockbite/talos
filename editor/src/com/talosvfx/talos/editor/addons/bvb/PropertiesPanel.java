package com.talosvfx.talos.editor.addons.bvb;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.logic.components.AComponent;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.ui.ContextualMenu;

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
        titleContainer.add(titleLabel).padTop(-titleLabel.getPrefHeight()-3).growX().left();

        if(propertyProvider instanceof AComponent){
            AComponent component = ((AComponent) propertyProvider);

            ImageButton settingButton = new ImageButton(TalosMain.Instance().getSkin().getDrawable("icon-edit"));
            settingButton.setSize(17, 17);
            titleContainer.add(settingButton).padTop(-titleLabel.getPrefHeight() - 3).right().row();

            ContextualMenu contextualMenu = new ContextualMenu();

            contextualMenu.addItem("Reset", new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    component.reset();
                    updateValues();
                }
            });

            contextualMenu.addItem("Remove", new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    component.remove();
                    PropertiesPanel.this.remove();
                }
            });

            settingButton.addListener( new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    contextualMenu.show(getStage());
                }
            });

        }else{
            titleContainer.row();
        }

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

    public void reconstruct () {
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
