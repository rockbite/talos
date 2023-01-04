package com.talosvfx.talos.editor.notifications.events.commands;

import com.talosvfx.talos.editor.notifications.commands.enums.Commands;
import lombok.Getter;
import lombok.Setter;

public class CommandEvent implements ICommandEvent {

    @Getter
    @Setter
    Commands.CommandType commandType;

    @Override
    public void reset() {
        commandType = null;
    }

}
