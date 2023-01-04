package com.talosvfx.talos.editor.notifications.events.actions;

import com.talosvfx.talos.editor.notifications.TalosEvent;
import com.talosvfx.talos.editor.notifications.actions.enums.Actions;

public interface IActionEvent extends TalosEvent {
    Actions.ActionType getActionType();
    void setActionType(Actions.ActionType type);
}
