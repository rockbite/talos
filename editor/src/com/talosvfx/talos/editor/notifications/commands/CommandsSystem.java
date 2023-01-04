package com.talosvfx.talos.editor.notifications.commands;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.TalosEvent;
import com.talosvfx.talos.editor.notifications.commands.enums.Commands;
import com.talosvfx.talos.editor.notifications.commands.implementations.GeneralCommand;
import com.talosvfx.talos.editor.notifications.events.commands.CommandContextEvent;
import com.talosvfx.talos.editor.notifications.events.commands.CommandEvent;
import com.talosvfx.talos.editor.notifications.events.commands.ICommandEvent;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CommandsSystem extends InputAdapter {

    private static final Logger logger = LoggerFactory.getLogger(CommandsSystem.class);

    private Array<ICommand> allCommands = new Array<>();

    private Array<InputAdapter> injectedAdapters = new Array<>();

    public void injectAdapter(InputAdapter inputAdapter) {
        injectedAdapters.add(inputAdapter);
    }

    public void removeAdapter(InputAdapter inputAdapter) {
        injectedAdapters.removeValue(inputAdapter, true);
    }

    public CommandsSystem() {

        KeyboardCombination copyCombination = new KeyboardCombination(Input.Keys.C, false, ModifierKey.CTRL);
        GeneralCommand copyAction = new GeneralCommand(Commands.CommandType.COPY, CommandContextType.FOCUSED_APP, copyCombination, null);
        allCommands.add(copyAction);

        KeyboardCombination saveCombination = new KeyboardCombination(Input.Keys.S, false, ModifierKey.CTRL);
        GeneralCommand saveAction = new GeneralCommand(Commands.CommandType.SAVE, CommandContextType.GLOBAL, saveCombination, null);
        allCommands.add(saveAction);

        KeyboardCombination openCombination = new KeyboardCombination(Input.Keys.O, false, ModifierKey.CTRL);
        GeneralCommand openAction = new GeneralCommand(Commands.CommandType.OPEN, CommandContextType.FOCUSED_APP, openCombination, null);
        allCommands.add(openAction);
    }

    private boolean checkCommandState() {
        boolean isRun = false;
        for (ICommand command : allCommands) {
            if (command.isReadyToRun()) {
                runCommand(command);
                isRun = true;
            }
        }

        if (isRun) {
            clearAfterRunning();
        }

        return isRun;
    }

    public void act(float delta) {
        for (ICommand command : allCommands) {
            command.getActiveCombination().act(delta);
        }
        checkCommandState();
    }

    public void runCommand(ICommand command) {
        Notifications.fireEvent(getEventForCommand(command));
        command.commandIsRun();
        logger.info("COMMAND IS RUN - " + command.getCommandType().name);
    }

    public void clearAfterRunning() {
        for (ICommand command : allCommands) {
            command.clearAfterRunning();
        }
    }

    private TalosEvent getEventForCommand(ICommand command) {
        ICommandEvent commandEvent = Notifications.obtainEvent(getCommandEventTypeForContextType(command.getContextType()));
        commandEvent.setCommandType(command.getCommandType());
        CommandContextType contextType = command.getContextType();

        if (contextType == CommandContextType.FOCUSED_APP) {
            CommandContextEvent contextEvent = (CommandContextEvent) commandEvent;
            AppManager appManager = SharedResources.appManager;
            AppManager.BaseApp focusedApp = appManager.getFocusedApp();
            contextEvent.setContext(focusedApp);
        }

        return commandEvent;
    }

    Class<? extends ICommandEvent> getCommandEventTypeForContextType(CommandContextType type) {
        switch (type) {
            case GLOBAL:
                return CommandEvent.class;
            case FOCUSED_APP:
                return CommandContextEvent.class;
        }

        return null;
    }


    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        for (ICommand command : allCommands) {
            command.getActiveCombination().mouseMoved();
        }

        for (InputAdapter inputAdapter : injectedAdapters) {
            inputAdapter.mouseMoved(screenX, screenY);
        }

        return checkCommandState();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        for (ICommand command : allCommands) {
            command.getActiveCombination().touchDown(button);
        }

        for (InputAdapter inputAdapter : injectedAdapters) {
            inputAdapter.touchDown(screenX, screenY, pointer, button);
        }

        return checkCommandState();
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        for (ICommand command : allCommands) {
            command.getActiveCombination().scrolled(amountY);
        }

        for (InputAdapter inputAdapter : injectedAdapters) {
            inputAdapter.scrolled(amountX, amountY);
        }

        return checkCommandState();
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        for (ICommand command : allCommands) {
            command.getActiveCombination().touchUp(button);
        }

        for (InputAdapter inputAdapter : injectedAdapters) {
            inputAdapter.touchUp(screenX, screenY, pointer, button);
        }

        return checkCommandState();
    }

    @Override
    public boolean keyDown(int keycode) {
        for (ICommand command : allCommands) {
            command.getActiveCombination().keyDown(keycode);
        }

        for (InputAdapter inputAdapter : injectedAdapters) {
            inputAdapter.keyDown(keycode);
        }

        return checkCommandState();
    }

    @Override
    public boolean keyUp(int keycode) {
        for (ICommand command : allCommands) {
            command.getActiveCombination().keyUp(keycode);
        }

        for (InputAdapter inputAdapter : injectedAdapters) {
            inputAdapter.keyUp(keycode);
        }

        return checkCommandState();
    }
}
