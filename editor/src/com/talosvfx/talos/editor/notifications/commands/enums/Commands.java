package com.talosvfx.talos.editor.notifications.commands.enums;

import java.lang.String;

/**
 * This is a generated class. It shouldn't be modified by hand, as the changes would be overridden.
 * To regenerate this class, call generateCommandsEnum task from Gradle.
 * The XML file is located in editor/assets/commands.xml
 */
public class Commands {
  public enum CommandGroup {
    GENERAL,

    TRANSFORM
  }

  public enum CommandType {
    COPY("copy_command", "Copy", "", "GENERAL"),

    CUT("cut_command", "Cut", "", "GENERAL"),

    PASTE("paste_command", "Paste", "", "GENERAL"),

    SAVE("save_command", "Save", "", "GENERAL"),

    EXPORT("export_command", "Export", "", "GENERAL"),

    EXPORT_OPTIMIZED("export_command_optimized", "Export Optimized", "", "GENERAL"),

    OPEN("open_command", "Open", "", "GENERAL"),

    UNDO("undo_command", "Undo", "", "GENERAL"),

    REDO("redo_command", "Redo", "", "GENERAL"),

    SELECT_ALL("select_all_command", "Select All", "", "GENERAL"),

    DELETE("delete_command", "Delete", "", "GENERAL"),

    RENAME("rename_command", "Rename", "", "GENERAL"),

    GROUP("group_command", "Group", "", "GENERAL"),

    UNGROUP("ungroup_command", "Ungroup", "", "GENERAL"),

    ESCAPE("escape_command", "Escape", "", "GENERAL"),

    LEFT("move_left_command", "Left", "", "TRANSFORM"),

    JUMPY_LEFT("move_jumpy_left_command", "Jumpy Left", "", "TRANSFORM"),

    RIGHT("move_right_command", "Right", "", "TRANSFORM"),

    JUMPY_RIGHT("move_jumpy_right_command", "Jumpy right", "", "TRANSFORM"),

    UP("move_up_command", "Up", "", "TRANSFORM"),

    JUMPY_UP("move_jumpy_up_command", "Jumpy up", "", "TRANSFORM"),

    DOWN("move_down_command", "Down", "", "TRANSFORM"),

    JUMPY_DOWN("move_jumpy_down_command", "Jumpy Down", "", "TRANSFORM");

    public final String uniqueName;

    public final String name;

    public final String description;

    public final CommandGroup commandType;

    CommandType(String uniqueName, String name, String description, String commandType) {
      this.uniqueName = uniqueName;
          this.name = name;
          this.description = description;
          this.commandType = CommandGroup.valueOf(commandType);;
    }
  }
}
