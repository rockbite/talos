package com.talosvfx.talos.editor.notifications.commands.enums;

import java.lang.String;

/**
 * This is a generated class. It shouldn't be modified by hand, as the changes would be overridden.
 * To regenerate this class, call generateActionsEnum task from Gradle.
 *  The XML file is located in editor/assets/commands.xml
 */
public class Commands {
  public enum CommandGroup {
    GENERAL
  }

  public enum CommandType {
    COPY(CommandGroup.GENERAL, "copy_action", "Copy", ""),
    SAVE(CommandGroup.GENERAL, "save_action", "Save", ""),
    OPEN(CommandGroup.GENERAL, "open_action", "Open", "");

    public final CommandGroup commandGroup;
    public final String uniqueName;
    public final String name;
    public final String description;

    CommandType(CommandGroup commandGroup, String uniqueName, String name, String description) {
      this.commandGroup = commandGroup;
      this.uniqueName = uniqueName;;
      this.name = name;
      this.description = description;
    }
  }
}
