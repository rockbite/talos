package com.talosvfx.talos.editor.widgets.ui.menu;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.project2.SharedResources;

public class MenuRow extends Table {

    private final Label label;

    Drawable selectedDrawable;

    ClickListener clickListener;

    public MenuRow() {
        label = new Label("", SharedResources.skin);

        Label shortcut = new Label("Ctrl+C", SharedResources.skin);

        add().padLeft(7).left().size(20);

        add(label).pad(2).padLeft(7).padRight(10).left().expandX();

        add(shortcut).padRight(7).right().expandX();

        selectedDrawable = SharedResources.skin.getDrawable("menu-selection-bg-blue");

        clickListener = new ClickListener();
        addListener(clickListener);

        setTouchable(Touchable.enabled);
    }

    public void buildFrom(XmlReader.Element item) {
        String title = item.getAttribute("title");

        label.setText(title);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if(clickListener.isOver()) {
            setBackground(selectedDrawable);
        } else {
            setBackground((Drawable) null);
        }
    }
}
