package com.talosvfx.talos.editor.project2;

import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerListener;
import com.talosvfx.talos.editor.dialogs.PreferencesWindow;
import com.talosvfx.talos.editor.dialogs.AboutTalosDialog;

public class UIController {

    private ColorPicker colorPicker;
    private PreferencesWindow preferencesWindow;
    private AboutTalosDialog aboutTalosDialog;

    public UIController() {
        initColorPicker();
        initPreferenceWindow();
        initAboutTalosDialog();
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

    private void initAboutTalosDialog() {
        aboutTalosDialog = new AboutTalosDialog();
    }

    public void showColorPicker(ColorPickerListener listener) {
        colorPicker.setListener(listener);
        SharedResources.stage.addActor(colorPicker.fadeIn());
    }

    public void showPreferencesWindow () {
        SharedResources.windowUtils.openWindow(preferencesWindow);
    }

    public void showAboutTalosDialog() {
        SharedResources.windowUtils.openWindow(aboutTalosDialog);
    }
}
