package com.talosvfx.talos.editor.notifications.events.actions;

import com.talosvfx.talos.editor.notifications.actions.enums.Actions;
import lombok.Getter;
import lombok.Setter;

public class ActionEvent implements IActionEvent {

    @Getter
    @Setter
    Actions.ActionType actionType;

    @Override
    public void reset() {
        actionType = null;
    }

}
