package com.talosvfx.talos.editor.notifications.actions.implementations;

import com.talosvfx.talos.editor.notifications.actions.Action;
import com.talosvfx.talos.editor.notifications.actions.ActionKeyCombination;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractAction implements Action {

    @Getter
    private final String name;
    @Getter
    private final String description;
    @Getter @Setter
    private String fullName;

    @Getter
    private final String uniqueName;

    @Getter
    private ActionKeyCombination actionKeyCombination;

    @Getter
    private final ActionKeyCombination defaultKeyCombination;

    private boolean isDefaultActionOverridden;

    public AbstractAction(String name, String fullName, String description, String uniqueName, ActionKeyCombination defaultKeyCombination, ActionKeyCombination overriddenKeyCombination) {
        this.name = name;
        this.fullName = fullName;
        this.description = description;
        this.uniqueName = uniqueName;
        this.defaultKeyCombination = defaultKeyCombination;
        this.isDefaultActionOverridden = overriddenKeyCombination != null;
        this.actionKeyCombination = isDefaultActionOverridden ? overriddenKeyCombination : defaultKeyCombination;
    }


    @Override
    public boolean isReadyToRun() {
        return actionKeyCombination.keyCombination.shouldExecute();
    }

    @Override
    public void runAction() {
        actionKeyCombination.keyCombination.actionIsRun();
    }

    @Override
    public void clearAfterRunning() {
        actionKeyCombination.keyCombination.resetState();
    }
}
