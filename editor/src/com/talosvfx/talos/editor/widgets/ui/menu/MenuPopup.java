package com.talosvfx.talos.editor.widgets.ui.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.project2.SharedResources;
import lombok.Getter;

public class MenuPopup extends Table {

    @Getter
    private String id;

    public MenuPopup(String id) {
        this.id = id;

        setBackground(SharedResources.skin.getDrawable("top-menu-popup-main-bg"));
    }

    public void buildFrom(XmlReader.Element parent, boolean isPrimary) {

        add().pad(2).row();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            XmlReader.Element item = parent.getChild(i);

            Table row = null;
            if(item.getName().equals("menu")) {
                MenuRow menuRow = new MenuRow();
                menuRow.buildFrom(item);

                row = menuRow;
            }

            if(row != null) {
                add(row).pad(0).padLeft(10).padRight(10).growX();
                row();
            }
        }

        add().pad(5).row();

        pack();
    }

    public String getId() {
        return id;
    }
}
