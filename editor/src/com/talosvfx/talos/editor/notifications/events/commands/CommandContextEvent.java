package com.talosvfx.talos.editor.notifications.events.commands;

import com.talosvfx.talos.editor.notifications.ContextRequiredEvent;
import com.talosvfx.talos.editor.notifications.commands.enums.Commands;
import com.talosvfx.talos.editor.project2.AppManager;
import lombok.Getter;
import lombok.Setter;

public class CommandContextEvent extends ContextRequiredEvent<AppManager.BaseApp> implements ICommandEvent {

    @Getter@Setter
    Commands.CommandType commandType;

    @Override
    public void reset() {
        super.reset();
        commandType = null;
    }
}
