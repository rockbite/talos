package com.talosvfx.talos.editor.dialogs.preference.widgets.blocks;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.editor.notifications.commands.Combination;

public abstract class InputCombinationTypeWidget<T extends Combination> extends Table {
    protected T currentCombination;

    public InputCombinationTypeWidget(T combination) {
        construct(combination);
    }

    public InputCombinationTypeWidget() {
        createEmpty();
        construct(currentCombination);
    }

    protected abstract void createEmpty();

    protected abstract void construct (T combination);
}
