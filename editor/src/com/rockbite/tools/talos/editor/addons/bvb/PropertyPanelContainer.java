package com.rockbite.tools.talos.editor.addons.bvb;

import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.rockbite.tools.talos.editor.widgets.propertyWidgets.IPropertyProvider;

public class PropertyPanelContainer extends Table {

    Table container;
    ScrollPane scrollPane;

    public PropertyPanelContainer(Skin skin) {
        setSkin(skin);
        container = new Table();
        scrollPane = new ScrollPane(container);

        add(scrollPane).grow();
    }

    public void clearPanes() {
        container.clear();
    }

    public void addPanel(IPropertyProvider propertyProvider) {
        PropertiesPanel panel = new PropertiesPanel(propertyProvider, getSkin());

        container.add(panel).growX().top().padBottom(5);
        container.row();
        container.add().expandY();
    }
}
