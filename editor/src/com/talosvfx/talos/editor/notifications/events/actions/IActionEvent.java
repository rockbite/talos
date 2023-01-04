package com.talosvfx.talos.editor.notifications.events.actions;

import com.talosvfx.talos.editor.notifications.TalosEvent;
import com.talosvfx.talos.editor.notifications.actions.enums.Actions;

public interface IActionEvent extends TalosEvent {
    Actions.ActionEnumInterface getActionType();
    void setActionType(Actions.ActionEnumInterface type);
}
