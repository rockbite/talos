package com.talosvfx.talos.editor.dialogs.preference.widgets.blocks;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.talosvfx.talos.editor.dialogs.preference.widgets.KeymapBox;
import com.talosvfx.talos.editor.notifications.commands.CombinationType;
import com.talosvfx.talos.editor.project2.SharedResources;

public class KeyboardCombinationTypeWidget extends InputCombinationTypeWidget{
    private Button mainKeyButton;
    // NOTE: secondary key button is removed for now, if needed can be added later
    // private Button secondaryKeyButton;

    public KeyboardCombinationTypeWidget() {
        super(CombinationType.KEYBOARD);
    }

    @Override
    protected void construct() {
        super.construct();

        // NOTE: actions are removed for now, if needed can be added later
//        final SelectBox actions = new SelectBox(Styles.keyInputWidgetSelectBoxStyle);

        final CheckBox repeatCheckBox = new CheckBox("Repeat", SharedResources.skin);
        repeatCheckBox.getImageCell().padRight(5);

        // init main key button
        mainKeyButton = new KeymapBox();
        mainKeyButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                mainButtonPressed();
            }
        });

        // NOTE: secondary key button is removed for now, if needed can be added later
        // init secondary key button
//            secondaryKeyButton = new KeymapBox();
//            secondaryKeyButton.addListener(new ClickListener() {
//                @Override
//                public void clicked(InputEvent event, float x, float y) {
//                    super.clicked(event, x, y);
//                    secondaryButtonPressed();
//                }
//            });

        // assemble
        defaults().space(10);
//            firstRow.add(actions).width(100);
        add(mainKeyButton).width(80);
//            firstRow.add(secondaryKeyButton).width(80);
        add(repeatCheckBox).expandX().right();
    }

    private void mainButtonPressed() {

    }

    // NOTE: secondary key button is removed for now, if needed can be added later
    //  private void secondaryButtonPressed() {}
}
