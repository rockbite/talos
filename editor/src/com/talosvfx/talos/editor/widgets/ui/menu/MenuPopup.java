package com.talosvfx.talos.editor.widgets.ui.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.project2.SharedResources;
import lombok.Getter;

public class MenuPopup extends Table {

    private final MainMenu mainMenu;
    @Getter
    private String id;

    public MenuPopup(MainMenu mainMenu, String id) {
        this.mainMenu = mainMenu;
        this.id = id;
    }

    public void buildFrom(XmlReader.Element parent, boolean isPrimary) {

        int childCount = parent.getChildCount();

        int topPad = 2;
        if(!isPrimary) {
            topPad = 6;
        }

        add().pad(2).padTop(topPad).row();

        for (int i = 0; i < childCount; i++) {
            XmlReader.Element item = parent.getChild(i);

            Table row = null;
            if(item.getName().equals("menu")) {
                MenuRow menuRow = new MenuRow(mainMenu, id + "/" + item.getAttribute("name"));
                menuRow.buildFrom(item);

                row = menuRow;
                add(row).pad(0).padLeft(10).padRight(10).growX();
            } else if(item.getName().equals("separator")) {
                row = makeSeparator();
                add(row).growX().height(1).pad(10).padTop(4).padBottom(4);
            } else if(item.getName().equals("inject")) {
                String injectorName = item.getAttribute("name");

                Table table = new Table();
                row = table;
                add(row).grow().minWidth(200);

                mainMenu.registerContainer(this, id, injectorName, table);
            }

            if(row != null) {
                row();
            }
        }

        add().pad(5).row();

        if(isPrimary) {
            setBackground(SharedResources.skin.getDrawable("top-menu-popup-main-bg"));
        } else {
            setBackground(SharedResources.skin.getDrawable("top-menu-popup-sub-bg"));
        }

        pack();
    }

    private Table makeSeparator() {
        Table table = new Table();

        table.setBackground(SharedResources.skin.getDrawable("white"));
        table.setColor(Color.valueOf("444444ff"));

        return table;
    }

    public String getId() {
        return id;
    }
}
