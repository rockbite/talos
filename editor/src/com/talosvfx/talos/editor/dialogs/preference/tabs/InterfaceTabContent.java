package com.talosvfx.talos.editor.dialogs.preference.tabs;

import com.talosvfx.talos.editor.widgets.ui.common.CollapsableWidget;

public class InterfaceTabContent extends PreferenceTabContent {

    public InterfaceTabContent() {
        padTop(1).defaults().space(2).top().growX();

        final CollapsableWidget display = new CollapsableWidget("Display");

        add(display).padTop(2);
        row();
        add().expandY();
    }
}
