package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class CustomList<T extends Actor> extends Table {

    Array<T> items;

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
        for(T item: items) {
            Table container = new Table();
            container.add(item).growX();
            add(container).growX().row();
        }
    }
}
