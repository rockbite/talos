package com.rockbite.tools.talos.editor.addons.bvb;

import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.rockbite.tools.talos.editor.widgets.propertyWidgets.IPropertyProvider;

import java.util.Comparator;

public class PropertyPanelContainer extends Table {

    Table container;
    Table fakeContainer;
    ScrollPane scrollPane;

    ObjectMap<Class, IPropertyProvider> providerSet = new ObjectMap<>();

    public PropertyPanelContainer(Skin skin) {
        setSkin(skin);
        container = new Table();
        fakeContainer = new Table();
        scrollPane = new ScrollPane(fakeContainer);
        add(scrollPane).grow();

        fakeContainer.add(container).growX().row();
        fakeContainer.add().expandY();
    }

    public void showPanel(IPropertyProvider propertyProvider) {
        providerSet.put(propertyProvider.getClass(), propertyProvider);
        build();
    }

    public void build() {
        container.clear();

        Array<IPropertyProvider> list = new Array<>();

        for(IPropertyProvider provider: providerSet.values()) {
            list.add(provider);
        }

        list.sort(new Comparator<IPropertyProvider>() {
            @Override
            public int compare(IPropertyProvider o1, IPropertyProvider o2) {
                return o1.getPriority()-o2.getPriority();
            }
        });

        for(IPropertyProvider provider: list) {
            PropertiesPanel panel = new PropertiesPanel(provider, getSkin());

            container.add(panel).growX().top().padBottom(5);
            container.row();
        }
    }

    public void hidePanel(IPropertyProvider propertyProvider) {
        providerSet.remove(propertyProvider.getClass());
        build();
    }
}
