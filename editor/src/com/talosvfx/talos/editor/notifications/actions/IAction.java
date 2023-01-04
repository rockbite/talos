package com.talosvfx.talos.editor.notifications.actions;

public interface IAction {
    String getName();

    String getDescription();

    String getFullName();

    void setFullName(String fullName);

    String getUniqueName();

    Combination getActiveCombination();

    Combination getDefaultCombination();

    boolean isReadyToRun();

    void clearAfterRunning();

    void runAction();
}
