package com.talosvfx.talos.editor.notifications.commands.implementations;

import com.badlogic.gdx.Input;
import com.talosvfx.talos.editor.notifications.commands.CommandContextType;
import com.talosvfx.talos.editor.notifications.commands.Combination;
import com.talosvfx.talos.editor.notifications.commands.ICommand;
import com.talosvfx.talos.editor.notifications.commands.enums.Commands;
import lombok.Getter;

public class GeneralCommand implements ICommand {
    @Getter
    private Combination activeCombination;

    private CommandContextType contextType;

    @Getter
    private final Combination defaultCombination;

    private boolean isDefaultCombinationOverridden;

    @Getter
    private Commands.CommandType commandType;

    public GeneralCommand(Commands.CommandType commandType, CommandContextType context, Combination defaultCombination) {
        this.commandType = commandType;
        this.defaultCombination = defaultCombination;
        this.contextType = context;
        this.isDefaultCombinationOverridden = false;
        this.activeCombination = defaultCombination;
    }

    public void overrideCombination (Combination combination) {
        this.isDefaultCombinationOverridden = true;
        this.activeCombination = combination;
    }

    @Override
    public CommandContextType getContextType() {
        return contextType;
    }

    @Override
    public boolean isReadyToRun() {
        return activeCombination.shouldExecute();
    }

    @Override
    public void commandIsRun() {
        activeCombination.commandIsRun();
    }

    @Override
    public void clearAfterRunning() {
        activeCombination.resetState();
    }
}
