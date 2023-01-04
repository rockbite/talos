package com.talosvfx.talos.editor.notifications.actions.implementations;

import com.talosvfx.talos.editor.notifications.actions.*;
import com.talosvfx.talos.editor.project2.SharedResources;

public class CopyAction extends AbstractAction {

    public CopyAction(ActionCombinationWrapper defaultKeyCombination, ActionCombinationWrapper overriddenKeyCombination) {
        super("Copy", "generic.copy",  "", "copy_action", defaultKeyCombination, overriddenKeyCombination);
    }

    @Override
    public void runAction() {
        super.runAction();
        SharedResources.globalSaveStateSystem.onUndoRequest();
    }
}
