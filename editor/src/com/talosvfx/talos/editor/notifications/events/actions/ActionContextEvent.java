package com.talosvfx.talos.editor.notifications.events.actions;

import com.talosvfx.talos.editor.notifications.ContextRequiredEvent;
import com.talosvfx.talos.editor.notifications.actions.enums.Actions;
import lombok.Getter;
import lombok.Setter;

public class ActionContextEvent extends ContextRequiredEvent<Object> implements IActionEvent {

    @Getter@Setter
    Actions.ActionEnumInterface actionType;

    @Override
    public void reset() {
        super.reset();
        actionType = null;
    }
}
