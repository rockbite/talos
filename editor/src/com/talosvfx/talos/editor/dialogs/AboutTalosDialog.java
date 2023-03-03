package com.talosvfx.talos.editor.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.TALOS_BUILD;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class AboutTalosDialog extends AWindowDialog implements Observer {
    @Override
    public Table build() {
        Table table = init();
        addVersionRow(table);
        table.pack();
        table.setSize(280, 60);

        return table;
    }

    private Table init() {
        Table table = new Table();
        table.setBackground(ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_BOTTOM, ColorLibrary.BackgroundColor.SUPER_DARK_GRAY));
        table.defaults().space(5);
        table.pad(10);
        return table;
    }

    private void addVersionRow (Table table) {
        // version info
        Label version = new Label("Version:", SharedResources.skin);
        Label versionNumber = new Label(TALOS_BUILD.getVersion(), SharedResources.skin);
        table.add(version);
        table.add(versionNumber).expandX().right();
        table.row();
    }

    @Override
    public String getTitle() {
        return "About Talos";
    }
}
