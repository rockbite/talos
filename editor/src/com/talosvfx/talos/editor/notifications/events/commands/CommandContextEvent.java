package com.talosvfx.talos.editor.notifications.events.commands;

import com.talosvfx.talos.editor.notifications.ContextRequiredEvent;
import com.talosvfx.talos.editor.notifications.commands.enums.Commands;
import com.talosvfx.talos.editor.project2.AppManager;
import lombok.Getter;
import lombok.Setter;

public class CommandContextEvent extends CommandEvent implements ContextRequiredEvent<AppManager.BaseApp> {
    @Getter@Setter
    AppManager.BaseApp context;

    @Override
    public void reset() {
        super.reset();
        context = null;
    }
}
