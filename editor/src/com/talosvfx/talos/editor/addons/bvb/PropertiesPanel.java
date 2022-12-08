package com.talosvfx.talos.editor.addons.bvb;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.events.ComponentRemoved;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.Scene;
import com.talosvfx.talos.editor.addons.scene.logic.components.AComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.widgets.PropertyPanel;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.ui.ContextualMenu;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesPanel extends Table {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesPanel.class);
    private final PropertyPanel parentPropertyPanel;


    Label titleLabel;
    Table propertyGroup = new Table();
    Array<IPropertyProvider> currentPropertyPanels = new Array<>();

    Array<PropertyWidget> propertyWidgets = new Array<>();

    public PropertiesPanel(IPropertyProvider propertyProvider, Skin skin, PropertyPanel propertyPanel) {
        parentPropertyPanel = propertyPanel;

        setSkin(skin);
        setBackground(skin.getDrawable("panel"));

        padLeft(5);
        padTop(25);

        setRound(false);

        titleLabel = new Label("", getSkin());
        titleLabel.setEllipsis(true);
        Table titleContainer = new Table();
        titleContainer.add(titleLabel).padTop(-titleLabel.getPrefHeight()-3).growX().left();

        if (propertyProvider instanceof AComponent && !(propertyProvider instanceof TransformComponent)) {
            AComponent component = ((AComponent) propertyProvider);

            ImageButton settingButton = new ImageButton(SharedResources.skin.getDrawable("icon-edit"));
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
                    GameObject gameObject = component.getGameObject();
                    if (gameObject != null) {

                        ComponentRemoved componentRemoved = Notifications.obtainEvent(ComponentRemoved.class);
                        componentRemoved.setComponent(component);
                        componentRemoved.setGameObject(gameObject);
                        componentRemoved.setContainer(parentPropertyPanel.getGameAsset().getResource());
                        Notifications.fireEvent(componentRemoved);


                    }
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
