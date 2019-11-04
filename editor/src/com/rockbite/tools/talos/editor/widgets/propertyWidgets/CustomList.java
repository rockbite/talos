package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.rockbite.tools.talos.editor.addons.bvb.AttachmentPoint;

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
            container.add(item).growX().padTop(2f).padBottom(2f);
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

    public void select(T itemToSelect) {
        for(T item: items) {
            unselect(item);
        }
        //containers.get(itemToSelect).setBackground(getSkin().getDrawable("panel_input_bg_selected"));
    }

    public void unselect(T item) {
        Drawable nullDrawable = null; // well. I have never encountered this problem before, so that's a first.
        containers.get(item).setBackground(nullDrawable);
    }
}
