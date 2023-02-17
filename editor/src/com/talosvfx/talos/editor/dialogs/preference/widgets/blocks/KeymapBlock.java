package com.talosvfx.talos.editor.dialogs.preference.widgets.blocks;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;

import com.talosvfx.talos.editor.dialogs.preference.widgets.PrefWidgetFactory;
import com.talosvfx.talos.editor.notifications.commands.Combination;
import com.talosvfx.talos.editor.notifications.commands.ICommand;
import com.talosvfx.talos.editor.notifications.commands.enums.Commands;
import com.talosvfx.talos.editor.project2.SharedResources;

public class KeymapBlock extends BlockWidget {

    public KeymapBlock() {
    }

    @Override
    public void build() {
        widgetArray = new Array<>();
        setBackground((Drawable) null);
        content.padLeft(10).defaults().space(4);

        getWidgetLabel().setText("Keymap");
        for (ICommand allCommand : SharedResources.commandsSystem.getAllCommands()) {
            String name = allCommand.getCommandType().name;
            PrefWidgetFactory.KeyInputWidget widget = new PrefWidgetFactory.KeyInputWidget(id + "." + name);
            widget.configure(allCommand);
            widgetArray.add(widget);

            content.add(widget).growX();
            content.row();
        }
    }
}
