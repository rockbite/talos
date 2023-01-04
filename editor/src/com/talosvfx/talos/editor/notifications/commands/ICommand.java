package com.talosvfx.talos.editor.notifications.commands;

import com.talosvfx.talos.editor.notifications.commands.enums.Commands;

public interface ICommand {
    Combination getActiveCombination();

    Combination getDefaultCombination();

    boolean isReadyToRun();

    void clearAfterRunning();

    void commandIsRun();

    CommandContextType getContextType();

    Commands.CommandType getCommandType();
}
