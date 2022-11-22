package com.talosvfx.talos.editor.widgets.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.talosvfx.talos.TalosMain;

public class ContextualMenu {

    PopupMenu popupMenu;

    public ContextualMenu() {
        build();
    }

    private void build () {
        popupMenu = new PopupMenu();
    }

    public void show (Stage stage) {
        final Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        stage.screenToStageCoordinates(vec);
        show(stage, vec.x, vec.y);
    }

    public void show (Stage stage, float x, float y) {
        popupMenu.showMenu(stage, x, y);
    }

    public void clearItems () {
        popupMenu.clearChildren();
    }

    public MenuItem addItem (String text, ClickListener listener) {
        MenuItem item = new MenuItem(text);
        item.addListener(listener);
        popupMenu.addItem(item);

        return item;
    }

    public void addSeparator () {
        popupMenu.addSeparator();
    }
}
