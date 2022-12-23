package com.talosvfx.talos.editor.dialogs.preference.tabs;

import com.talosvfx.talos.editor.widgets.ui.common.CollapsableWidget;

public class FilePathsTabContent extends PreferenceTabContent {
    public FilePathsTabContent () {
        padTop(1).defaults().space(2).top().growX();

        final CollapsableWidget data = new CollapsableWidget("Data");
        final CollapsableWidget render = new CollapsableWidget("Render");
        final CollapsableWidget applications = new CollapsableWidget("Applications");
        final CollapsableWidget assetLibraries = new CollapsableWidget("AssetLibraries");

        add(data);
        row();
        add(render);
        row();
        add(applications);
        row();
        add(assetLibraries);
        row();
        add().expandY();
    }
}
