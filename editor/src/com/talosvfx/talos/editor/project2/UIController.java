package com.talosvfx.talos.editor.project2;

import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerListener;
import com.talosvfx.talos.editor.dialogs.PreferencesWindow;

public class UIController {

    private ColorPicker colorPicker;
    private PreferencesWindow preferencesWindow;

    public UIController() {
        initColorPicker();
        initPreferenceWindow();
    }

    private void initColorPicker() {
        colorPicker = new ColorPicker();
		colorPicker.padTop(32);
		colorPicker.padLeft(16);
		colorPicker.setHeight(330);
		colorPicker.setWidth(430);
		colorPicker.padRight(26);
    }

    private void initPreferenceWindow() {
        preferencesWindow = new PreferencesWindow();
    }

    public void showColorPicker(ColorPickerListener listener) {
        colorPicker.setListener(listener);
        SharedResources.stage.addActor(colorPicker.fadeIn());
    }

    public void showPreferencesWindow () {
        System.out.println("showing preferences");
        SharedResources.stage.addActor(preferencesWindow);
    }
}
