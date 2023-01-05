package com.talosvfx.talos.editor.dialogs.preference.widgets;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.talosvfx.talos.editor.project2.SharedResources;

public class KeymapBox extends Button {
    private String keyName;
    private Label keyLabel;

    private boolean inWaitingMode;
    private boolean isOnlyUppercase = true;

    public KeymapBox () {
        super(SharedResources.skin, "square");
        constructContent();
        addListeners();
    }


    private void constructContent () {
        keyLabel = new Label("", SharedResources.skin, "small");
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

                // TODO: 05.01.23 not sure if focus should be given like this
                SharedResources.stage.setKeyboardFocus(KeymapBox.this);
            }

            @Override
            public boolean keyTyped(InputEvent event, char character) {
                if (inWaitingMode) {
                    String key = Character.toString(character);

                    if (isOnlyUppercase) key = key.toUpperCase();

                    setKey(key);

                    inWaitingMode = false;
                }
                return super.keyTyped(event, character);
            }
        });
    }

    public void setKey (String keyName) {
        setChecked(false);
        this.keyName = keyName;
        keyLabel.setText(keyName);
    }

    private void enterWaitingForInputMode () {
        keyLabel.setText("Press a key");
    }

    private void resetDefaults () {
        keyLabel.setText(keyName);
    }
}
