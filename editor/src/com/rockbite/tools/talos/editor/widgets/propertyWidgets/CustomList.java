package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.rockbite.tools.talos.editor.addons.bvb.AttachmentPoint;

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
            container.add(item).growX().padTop(2f).padBottom(2f);
            add(container).growX().row();
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
