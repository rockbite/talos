package com.talosvfx.talos.editor.dialogs.preference.widgets.blocks;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.editor.notifications.commands.CombinationType;

public class InputCombinationTypeWidget extends Table {
    private CombinationType combinationType;
    public InputCombinationTypeWidget(CombinationType combinationType) {
        this.combinationType = combinationType;
        construct();
    }

    protected void construct () {

    }
}
