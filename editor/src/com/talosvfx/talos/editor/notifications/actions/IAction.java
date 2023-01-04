package com.talosvfx.talos.editor.notifications.actions;

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
}
