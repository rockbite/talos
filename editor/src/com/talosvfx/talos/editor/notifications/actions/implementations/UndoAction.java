package com.talosvfx.talos.editor.notifications.actions.implementations;

import com.talosvfx.talos.editor.notifications.actions.*;
import com.talosvfx.talos.editor.project2.SharedResources;

public class UndoAction extends AbstractAction {

    public UndoAction(Combination defaultKeyCombination, Combination overriddenKeyCombination) {
        super("Undo", "generic.undo",  "", "undo_action", ActionContextType.GENERAL, defaultKeyCombination, overriddenKeyCombination);
    }

    @Override
    public void runAction() {
        super.runAction();
        SharedResources.globalSaveStateSystem.onUndoRequest();
    }
}
