package com.talosvfx.talos.editor.notifications.actions;

public interface Action {
    String getName();

    String getDescription();

    String getFullName();

    void setFullName(String fullName);

    String getUniqueName();

    ActionKeyCombination getActionKeyCombination();

    ActionKeyCombination getDefaultKeyCombination();

    boolean isReadyToRun();

    void clearAfterRunning();

    void runAction();
}
