package com.talosvfx.talos.editor.notifications.commands;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.notifications.commands.enums.Commands;
import com.talosvfx.talos.editor.notifications.commands.implementations.GeneralCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CommandRepository {
    private static final Logger logger = LoggerFactory.getLogger(CommandsSystem.class);
    public static ObjectMap<Commands.CommandType, ICommand> commandConfiguration = new ObjectMap<>();
    public static ObjectMap<Commands.CommandGroup, Array<Commands.CommandType>> commandGroupConfiguration = new ObjectMap<>();

    public void parseCommands(FileHandle file) {
        XmlReader xmlReader = new XmlReader();
        XmlReader.Element root = xmlReader.parse(file);

        parseDefaultActions(root);
    }

    private void parseDefaultActions(XmlReader.Element root) {
        Array<XmlReader.Element> groups = root.getChildrenByName("commandGroup");
        for (XmlReader.Element group : groups) {
            Array<Commands.CommandType> commandGroupTypes = new Array<>();
            commandGroupConfiguration.put(Commands.CommandGroup.valueOf(group.getAttribute("package").toUpperCase()), commandGroupTypes);
            Array<XmlReader.Element> commands = group.getChildrenByName("command");
            for (XmlReader.Element command : commands) {
                Commands.CommandType commandType = getCommandTypeByUniqueName(command.getAttribute("uniqueName"));
                commandGroupTypes.add(commandType);
                GeneralCommand generalCommand = new GeneralCommand(commandType, getContextType(command.getChildByName("context").getText()), parseCombination(command.getChildByName("combination")), null);
                commandConfiguration.put(commandType, generalCommand);
            }
        }
    }

    public Commands.CommandType getCommandTypeByUniqueName (String uniqueName) {
        for (Commands.CommandType value : Commands.CommandType.values()) {
            if (value.uniqueName.equals(uniqueName)) {
                return value;
            }
        }

        return null;
    }

    private CommandContextType getContextType (String type) {
        if (type.toUpperCase().equals("FOCUSED")) {
            return CommandContextType.FOCUSED_APP;
        } else if (type.toUpperCase().equals("GLOBAL")) {
            return CommandContextType.GLOBAL;
        }

        return null;
    }

    private Combination parseCombination (XmlReader.Element combinationElement) {
        String type = combinationElement.getAttribute("type");
        Combination combination;
        String commandText = combinationElement.getText();
        String[] keyStrings = commandText.split(" ");
        if (keyStrings.length < 1) {
            logger.error("WRONG CONFIGURATION FOR COMMAND");
            throw new GdxRuntimeException("Command combination should at least be 1 key - any modifier and 1 primary key");
        }

        ModifierKey[] modifierKeys = new ModifierKey[keyStrings.length - 1];
        for (int i = 0; i < keyStrings.length - 1; i++) {
            String keyString = keyStrings[i];
            modifierKeys[i] = (ModifierKey.valueOf(keyString.toUpperCase()));
        }

        if (type.equalsIgnoreCase("keyboard")) {
            String primaryKeyString = keyStrings[keyStrings.length - 1];
            int primaryKey = Input.Keys.valueOf(primaryKeyString);
            boolean repeat = combinationElement.getBooleanAttribute("repeat");
            combination = new KeyboardCombination(primaryKey, repeat, modifierKeys);
        } else {
            String mouseKeyStringName = keyStrings[keyStrings.length - 1];
            combination = new MouseCombination(MouseCommand.valueOf(mouseKeyStringName.toUpperCase()), modifierKeys);
        }

        return combination;
    }
}
