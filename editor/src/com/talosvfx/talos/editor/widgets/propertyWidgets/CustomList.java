package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class CustomList<T extends Actor> extends Table {

    Array<T> items;
    ObjectMap<T, Table> containers = new ObjectMap<>();

    public CustomList(Skin skin) {
        setSkin(skin);

        items = new Array<>();
    }

    public void addItem(T item) {
        items.add(item);
        rebuild();
    }

    public void removeItem(T item) {
        items.removeValue(item, true);
        rebuild();
    }

    public void rebuild() {
        clearChildren();
        containers.clear();
        for(T item: items) {
            Table container = new Table();
            container.add(item).growX().pad(2f).padLeft(4f).padRight(4f);
            add(container).growX().row();
            containers.put(item, container);
        }

        add().expandY();
    }

    public void clearItems() {
        clearItems(true);
    }

    public void clearItems(boolean rebuild) {
        items.clear();
        if(rebuild) {
            rebuild();
        }
    }

    public void addAll(Array<T> items) {
        items.addAll(items);
        rebuild();
    }
}
