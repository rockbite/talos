package com.talosvfx.talos.editor.notifications.actions.implementations;

import com.talosvfx.talos.editor.notifications.actions.ActionContextType;
import com.talosvfx.talos.editor.notifications.actions.Combination;
import com.talosvfx.talos.editor.notifications.actions.IAction;
import com.talosvfx.talos.editor.notifications.actions.enums.Actions;
import lombok.Getter;

public class GeneralAction implements IAction {

    @Getter
    private Combination activeCombination;

    private ActionContextType contextType;

    @Getter
    private final Combination defaultCombination;

    private boolean isDefaultActionOverridden;

    @Getter
    private Actions.ActionType actionType;

    public GeneralAction(Actions.ActionType actionType, ActionContextType context, Combination defaultCombination, Combination overriddenKeyCombination) {
        this.actionType = actionType;
        this.defaultCombination = defaultCombination;
        this.contextType = context;
        this.isDefaultActionOverridden = overriddenKeyCombination != null;
        this.activeCombination = isDefaultActionOverridden ? overriddenKeyCombination : defaultCombination;
    }

    @Override
    public ActionContextType getContextType() {
        return contextType;
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
