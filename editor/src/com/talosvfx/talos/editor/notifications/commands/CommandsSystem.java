package com.talosvfx.talos.editor.notifications.commands;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.FocusManager;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.TalosEvent;
import com.talosvfx.talos.editor.notifications.commands.enums.Commands;
import com.talosvfx.talos.editor.notifications.events.commands.CommandContextEvent;
import com.talosvfx.talos.editor.notifications.events.commands.CommandEvent;
import com.talosvfx.talos.editor.notifications.events.commands.ICommandEvent;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.utils.Toasts;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CommandsSystem extends InputAdapter {

    private static final Logger logger = LoggerFactory.getLogger(CommandsSystem.class);

    private CommandParser commandParser;

    @Getter
    private Array<ICommand> allCommands = new Array<>();

    public CommandsSystem() {
        commandParser = new CommandParser();
        commandParser.parseCommands(Gdx.files.local("commands.xml"));
        for (ObjectMap.Entry<Commands.CommandType, Array<ICommand>> entry : CommandParser.commandConfiguration) {
            allCommands.addAll(entry.value);
        }
    }
    private boolean checkCommandState() {
        boolean isRun = false;
        for (ICommand command : allCommands) {
            if (command.isReadyToRun()) {
                isRun = runCommand(command);
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

    public boolean runCommand(ICommand command) {
        Actor keyboardFocus = SharedResources.inputHandling.keyboardFocus;
        if (keyboardFocus instanceof TextField) {
            return false;
        }

        Notifications.fireEvent(getEventForCommand(command));
        command.commandIsRun();
        Toasts.getInstance().showInfoToast("Command - " + command.getCommandType().name);

        return true;
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
