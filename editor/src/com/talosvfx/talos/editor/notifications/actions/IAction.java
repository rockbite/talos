package com.talosvfx.talos.editor.notifications.actions;

import com.talosvfx.talos.editor.notifications.actions.enums.Actions;

public interface IAction {
    Combination getActiveCombination();

    Combination getDefaultCombination();

    boolean isReadyToRun();

    void clearAfterRunning();

    void runAction();

    ActionContextType getContextType();

    Actions.ActionType getActionType();
}
