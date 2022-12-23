package com.talosvfx.talos.editor.dialogs.preference.tabs;

import com.talosvfx.talos.editor.widgets.ui.common.CollapsableWidget;

public class AddOnsTabContent extends PreferenceTabContent {
    public AddOnsTabContent () {
        padTop(1).defaults().space(2);

        // example of adding collapsable widgets
        CollapsableWidget collapsableWidget = new CollapsableWidget();
        CollapsableWidget collapsableWidget2 = new CollapsableWidget();
        CollapsableWidget collapsableWidget3 = new CollapsableWidget();

        add(collapsableWidget).top().growX();
        row();
        add(collapsableWidget2).top().growX();
        row();
        add(collapsableWidget3).top().growX();
        row();
        add().expandY();
    }
}
