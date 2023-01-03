package com.talosvfx.talos.editor.dialogs.preference.widgets.blocks;

import com.badlogic.gdx.utils.Array;

import com.talosvfx.talos.editor.dialogs.preference.widgets.PrefWidgetFactory;
import com.talosvfx.talos.editor.notifications.actions.GlobalActions;

public class KeymapBlock extends BlockWidget {

    public KeymapBlock() {
    }

    @Override
    public void build() {
        widgetArray = new Array<>();
        getWidgetLabel().setText("Keymap");

        GlobalActions[] values = GlobalActions.values();

        for(int i = 0; i  < values.length; i++) {
            String name = values[i].name();
            PrefWidgetFactory.KeyInputWidget widget = new PrefWidgetFactory.KeyInputWidget(id + "." + name);
            widget.configure(values[i]);

            widgetArray.add(widget);

            content.add(widget).growX();
            content.row();
        }

    }
}
