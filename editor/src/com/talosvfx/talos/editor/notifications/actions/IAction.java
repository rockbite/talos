package com.talosvfx.talos.editor.notifications.actions;

import com.talosvfx.talos.editor.notifications.actions.enums.Actions;

public interface IAction {
    String getName();

    String getDescription();

    String getFullName();

    String getUniqueName();

    Combination getActiveCombination();

    Combination getDefaultCombination();

    boolean isReadyToRun();

    void clearAfterRunning();

    void runAction();

    ActionContextType getContextType();

    Actions.ActionEnumInterface getActionType();
}
