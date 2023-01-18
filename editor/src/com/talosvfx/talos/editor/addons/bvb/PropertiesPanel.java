package com.talosvfx.talos.editor.addons.bvb;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.events.ComponentRemoved;
import com.talosvfx.talos.runtime.scene.GameObject;import com.talosvfx.talos.runtime.scene.GameObjectContainer;
import com.talosvfx.talos.editor.addons.scene.logic.componentwrappers.AComponent;
import com.talosvfx.talos.editor.addons.scene.logic.componentwrappers.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.widgets.PropertyPanel;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.ui.menu.BasicPopup;
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

            ImageButton settingButton = new ImageButton(SharedResources.skin.getDrawable("ic-vertical-dots"));
            titleContainer.add(settingButton).padTop(-titleLabel.getPrefHeight() - 3).right().width(20).padRight(2)
                    .row();

            settingButton.addListener( new ClickListener(){

                private Vector2 temp = new Vector2();

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);

                    temp.set(x, y);
                    settingButton.localToScreenCoordinates(temp);

                    BasicPopup.build(String.class)
                            .addItem("Reset", "reset")
                            .addItem("Remove", "remove")
                            .onClick(new BasicPopup.PopupListener<String>() {
                                @Override
                                public void itemClicked(String payload) {
                                    if(payload.equals("reset")) {
                                        component.reset();
                                        updateValues();
                                    } else if(payload.equals("remove")) {
                                        component.remove();
                                        GameObject gameObject = component.getGameObject();
                                        if (gameObject != null) {

                                            if (parentPropertyPanel.getCurrentHolder() instanceof GameObjectContainer) {
                                                ComponentRemoved componentRemoved = Notifications.obtainEvent(ComponentRemoved.class);
                                                componentRemoved.setComponent(component);
                                                componentRemoved.setGameObject(gameObject);
                                                componentRemoved.setContainer((GameObjectContainer)parentPropertyPanel.getCurrentHolder());
                                                Notifications.fireEvent(componentRemoved);
                                            }
                                        }
                                        PropertiesPanel.this.remove();
                                    }
                                }
                            })
                            .show(settingButton, temp.x, temp.y);
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
                    propertyWidget.setTopLevelPropertiesPanel(parentPropertyPanel);
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
