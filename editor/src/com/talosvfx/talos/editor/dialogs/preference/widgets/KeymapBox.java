package com.talosvfx.talos.editor.dialogs.preference.widgets;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class KeymapBox extends Table {
    private String keyName;
    private Label keyLabel;

    private boolean inWaitingMode;

    private Drawable defaultBackgroundDrawable = ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_2, ColorLibrary.BackgroundColor.PANEL_GRAY);
    private Drawable clickedBackgroundDrawable = ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_2, ColorLibrary.BackgroundColor.LIGHT_BLUE);

    public KeymapBox () {
        constructContent();
        addListeners();
    }

    private void constructContent () {
        setBackground(defaultBackgroundDrawable);
        keyLabel = new Label("---", SharedResources.skin, "small");
        keyLabel.setAlignment(Align.center);
        add(keyLabel).growX().center();
    }


    private void addListeners() {
        setTouchable(Touchable.enabled);
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                inWaitingMode = !inWaitingMode;

                if (inWaitingMode) {
                    enterWaitingForInputMode();
                } else {
                    resetDefaults();
                }
            }
        });

        // listen to key input
//            // TODO: 27.12.22 do key binding
//            if (inWaitingMode) {
//                setKey(character);
//                inWaitingMode = false;
//            }
//
//            System.out.println(character);
    }

    public void setKey (String keyName) {
        setBackground(defaultBackgroundDrawable);
        this.keyName = keyName;
        keyLabel.setText(keyName);
    }

    private void enterWaitingForInputMode () {
        setBackground(clickedBackgroundDrawable);
        keyLabel.setText("Press a key");
    }

    private void resetDefaults () {
        setBackground(defaultBackgroundDrawable);
        keyLabel.setText(keyName);
    }
}
