package com.talosvfx.talos.editor.notifications.commands;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
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

    private CommandRepository commandRepository;

    private Array<ICommand> allCommands = new Array<>();

    public CommandsSystem() {
        commandRepository = new CommandRepository();
        commandRepository.parseCommands(Gdx.files.local("commands.xml"));
        for (ObjectMap.Entry<Commands.CommandType, ICommand> entry : CommandRepository.commandConfiguration) {
            allCommands.add(entry.value);
        }
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

        return checkCommandState();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        for (ICommand command : allCommands) {
            command.getActiveCombination().touchDown(button);
        }

        return checkCommandState();
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        for (ICommand command : allCommands) {
            command.getActiveCombination().scrolled(amountY);
        }

        return checkCommandState();
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        for (ICommand command : allCommands) {
            command.getActiveCombination().touchUp(button);
        }

        return checkCommandState();
    }

    @Override
    public boolean keyDown(int keycode) {
        for (ICommand command : allCommands) {
            command.getActiveCombination().keyDown(keycode);
        }

        return checkCommandState();
    }

    @Override
    public boolean keyUp(int keycode) {
        for (ICommand command : allCommands) {
            command.getActiveCombination().keyUp(keycode);
        }

        return checkCommandState();
    }
}
