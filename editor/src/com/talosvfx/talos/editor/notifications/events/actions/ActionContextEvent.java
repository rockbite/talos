package com.talosvfx.talos.editor.notifications.events.actions;

import com.talosvfx.talos.editor.notifications.ContextRequiredEvent;
import com.talosvfx.talos.editor.notifications.actions.enums.Actions;
import com.talosvfx.talos.editor.project2.AppManager;
import lombok.Getter;
import lombok.Setter;

public class ActionContextEvent extends ContextRequiredEvent<AppManager.BaseApp> implements IActionEvent {

    @Getter@Setter
    Actions.ActionType actionType;

    @Override
    public void reset() {
        super.reset();
        actionType = null;
    }
}
