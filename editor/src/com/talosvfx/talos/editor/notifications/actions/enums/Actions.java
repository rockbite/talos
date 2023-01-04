package com.talosvfx.talos.editor.notifications.actions.enums;

import java.lang.String;

/**
 * This is a generated class. It shouldn't be modified by hand, as the changes would be overridden.
 * To regenerate this class, call generateActionsEnum task from Gradle.
 *  The XML file is located in editor/assets/actions.xml
 */
public class Actions {
  public interface ActionEnumInterface {
  }

  enum GeneralAction implements ActionEnumInterface {
    Copy("copy_action");

    public final String uniqueName;

    GeneralAction(String uniqueName) {
      this.uniqueName = uniqueName;;
    }
  }

  public static ActionEnumInterface[] ALL_ACTIONS = new ActionEnumInterface[]{GeneralAction.Copy};
}
