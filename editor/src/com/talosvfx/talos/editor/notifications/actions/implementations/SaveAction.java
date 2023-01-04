package com.talosvfx.talos.editor.notifications.actions.implementations;

import com.talosvfx.talos.editor.addons.scene.events.save.SaveRequest;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.actions.ActionCombinationWrapper;

public class SaveAction extends AbstractAction {
    public SaveAction(ActionCombinationWrapper defaultKeyCombination, ActionCombinationWrapper overriddenKeyCombination) {
        super("Save", "generic.save",  "", "save_action", defaultKeyCombination, overriddenKeyCombination);
    }
    @Override
    public void runAction() {
        super.runAction();
        SaveRequest saveRequest = Notifications.obtainEvent(SaveRequest.class);
        Notifications.fireEvent(saveRequest);
    }
}