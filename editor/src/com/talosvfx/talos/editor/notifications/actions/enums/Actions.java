package com.talosvfx.talos.editor.notifications.actions.enums;

import java.lang.String;

/**
 * This is a generated class. It shouldn't be modified by hand, as the changes would be overridden.
 * To regenerate this class, call generateActionsEnum task from Gradle.
 *  The XML file is located in editor/assets/actions.xml
 */
public class Actions {
  public enum ActionGroup {
    GENERAL
  }

  public enum ActionType {
    COPY(ActionGroup.GENERAL, "copy_action", "Copy", ""),
    SAVE(ActionGroup.GENERAL, "save_action", "Save", ""),
    OPEN(ActionGroup.GENERAL, "open_action", "Open", "");

    public final ActionGroup actionGroup;
    public final String uniqueName;
    public final String name;
    public final String description;

    ActionType (ActionGroup actionGroup, String uniqueName, String name, String description) {
      this.actionGroup = actionGroup;
      this.uniqueName = uniqueName;;
      this.name = name;
      this.description = description;
    }
  }
}
