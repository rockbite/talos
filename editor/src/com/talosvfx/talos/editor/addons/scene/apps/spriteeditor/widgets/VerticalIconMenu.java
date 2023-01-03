package com.talosvfx.talos.editor.addons.scene.apps.spriteeditor.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import lombok.Getter;

import java.util.ArrayList;

public class VerticalIconMenu<T extends Actor, W extends Actor> extends Table {
    @Getter
    private final ObjectMap<T, W> tabWindowMap;
    @Getter
    private final ArrayList<T> tabs;

    private final Table container;
    public VerticalIconMenu() {
        this.tabWindowMap = new ObjectMap<>();
        this.tabs = new ArrayList<>();

        setBackground(ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_BOTTOM, ColorLibrary.BackgroundColor.ULTRA_DARK_GRAY));

        container = new Table();
        container.defaults().space(3);

        add(container).growX();
        row();
        add().expandY();
    }

    public W getWindow (T tab) {
        return tabWindowMap.get(tab);
    }

    public Cell addTab (T tab, W window) {
        tabs.add(tab);
        tabWindowMap.put(tab, window);
        tab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);

                final ChangeListener.ChangeEvent changeEvent = Pools.obtain(ChangeListener.ChangeEvent.class);
                tab.fire(changeEvent);
                Pools.free(changeEvent);
            }
        });
        final Cell cell = container.add(tab).growX();
        container.row();
        return cell;
    }
}
