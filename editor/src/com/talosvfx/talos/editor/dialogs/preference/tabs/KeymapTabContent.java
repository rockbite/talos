package com.talosvfx.talos.editor.dialogs.preference.tabs;

import com.talosvfx.talos.editor.widgets.ui.common.KeymapRowWidget;

public class KeymapTabContent extends PreferenceTabContent {
    public KeymapTabContent () {
        final KeymapRowWidget keymapRowWidget = new KeymapRowWidget("test");

        add(keymapRowWidget).growX().row();
        add().expandY();
    }
}
