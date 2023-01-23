package com.talosvfx.talos.editor.dialogs.preference.widgets.blocks;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.talosvfx.talos.editor.dialogs.preference.widgets.KeymapBox;
import com.talosvfx.talos.editor.notifications.commands.KeyboardCombination;
import com.talosvfx.talos.editor.project2.SharedResources;

public class KeyboardCombinationTypeWidget extends InputCombinationTypeWidget<KeyboardCombination> {
    private KeymapBox mainKeyButton;

    private CheckBox repeatCheckBox;

    public KeyboardCombinationTypeWidget(KeyboardCombination combination) {
        super(combination);
    }

    public KeyboardCombinationTypeWidget() {
        super();
    }

    @Override
    protected void createEmpty() {
        currentCombination = new KeyboardCombination(Input.Keys.A, false);
    }

    @Override
    protected void construct(KeyboardCombination combination) {
        repeatCheckBox = new CheckBox("Repeat", SharedResources.skin);
        repeatCheckBox.getImageCell().padRight(5);
        repeatCheckBox.setChecked(combination.isRepeat());

        // init main key button
        mainKeyButton = new KeymapBox();
        mainKeyButton.setKey(combination.getRegularKey());

        // assemble
        defaults().space(10);
        add(mainKeyButton).width(80);
        add(repeatCheckBox).expandX().right();
    }
}
