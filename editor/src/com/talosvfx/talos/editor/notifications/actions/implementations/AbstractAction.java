package com.talosvfx.talos.editor.notifications.actions.implementations;

import com.talosvfx.talos.editor.notifications.actions.IAction;
import com.talosvfx.talos.editor.notifications.actions.ActionCombinationWrapper;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractAction implements IAction {

    @Getter
    private final String name;
    @Getter
    private final String description;
    @Getter @Setter
    private String fullName;

    @Getter
    private final String uniqueName;

    @Getter
    private ActionCombinationWrapper actionCombinationWrapper;

    @Getter
    private final ActionCombinationWrapper defaultKeyCombination;

    private boolean isDefaultActionOverridden;

    public AbstractAction(String name, String fullName, String description, String uniqueName, ActionCombinationWrapper defaultKeyCombination, ActionCombinationWrapper overriddenKeyCombination) {
        this.name = name;
        this.fullName = fullName;
        this.description = description;
        this.uniqueName = uniqueName;
        this.defaultKeyCombination = defaultKeyCombination;
        this.isDefaultActionOverridden = overriddenKeyCombination != null;
        this.actionCombinationWrapper = isDefaultActionOverridden ? overriddenKeyCombination : defaultKeyCombination;
    }


    @Override
    public boolean isReadyToRun() {
        return actionCombinationWrapper.combination.shouldExecute();
    }

    @Override
    public void runAction() {
        actionCombinationWrapper.combination.actionIsRun();
    }

    @Override
    public void clearAfterRunning() {
        actionCombinationWrapper.combination.resetState();
    }
}
