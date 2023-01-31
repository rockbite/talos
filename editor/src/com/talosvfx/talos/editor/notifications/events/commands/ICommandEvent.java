package com.talosvfx.talos.editor.notifications.events.commands;

import com.talosvfx.talos.editor.notifications.TalosEvent;
import com.talosvfx.talos.editor.notifications.commands.enums.Commands;

public interface ICommandEvent extends TalosEvent {
    Commands.CommandType getCommandType();
    void setCommandType(Commands.CommandType type);
}
