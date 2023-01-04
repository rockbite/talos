package com.talosvfx.talos.editor.notifications.actions.implementations;

import com.talosvfx.talos.editor.notifications.actions.Combination;
import com.talosvfx.talos.editor.notifications.actions.IAction;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractAction implements IAction {

    @Getter
    private final String name;
    @Getter
    private final String description;
    @Getter
    private String fullName;

    @Getter
    private final String uniqueName;

    @Getter
    private Combination activeCombination;

    @Getter
    private final Combination defaultCombination;

    private boolean isDefaultActionOverridden;

    public AbstractAction(String name, String fullName, String description, String uniqueName, Combination defaultCombination, Combination overriddenKeyCombination) {
        this.name = name;
        this.fullName = fullName;
        this.description = description;
        this.uniqueName = uniqueName;
        this.defaultCombination = defaultCombination;
        this.isDefaultActionOverridden = overriddenKeyCombination != null;
        this.activeCombination = isDefaultActionOverridden ? overriddenKeyCombination : defaultCombination;
    }

    @Override
    public boolean isReadyToRun() {
        return activeCombination.shouldExecute();
    }

    @Override
    public void runAction() {
        activeCombination.actionIsRun();
    }

    @Override
    public void clearAfterRunning() {
        activeCombination.resetState();
    }
}
