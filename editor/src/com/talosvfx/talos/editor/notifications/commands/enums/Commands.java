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

    MOVE_LEFT_1PX("moveLeft", "Move left 1px", "", "TRANSFORM"),

    MOVE_LEFT_10PX("moveJumpyLeft", "Move left 10px", "", "TRANSFORM"),

    MOVE_RIGHT_1X("moveRight", "Move right 1x", "", "TRANSFORM"),

    MOVE_RIGHT_10PX("moveJumpyRight", "Move right 10px", "", "TRANSFORM"),

    MOVE_UP_1PX("moveUp", "Move Up 1px", "", "TRANSFORM"),

    MOVE_UP_10PX("moveJumpyUp", "Move Up 10px", "", "TRANSFORM"),

    MOVE_DOWN_1PX("moveDown", "Move Down 1px", "", "TRANSFORM"),

    MOVE_DOWN_10PX("moveJumpyDown", "Move Down 10px", "", "TRANSFORM");

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
