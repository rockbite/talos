package com.talosvfx.talos.editor.project2;

import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerListener;

public class UIController {

    ColorPicker colorPicker;

    public UIController() {
        initColorPicker();
    }

    private void initColorPicker() {
        colorPicker = new ColorPicker();
		colorPicker.padTop(32);
		colorPicker.padLeft(16);
		colorPicker.setHeight(330);
		colorPicker.setWidth(430);
		colorPicker.padRight(26);
    }

    public void showColorPicker(ColorPickerListener listener) {
        colorPicker.setListener(listener);
        SharedResources.stage.addActor(colorPicker.fadeIn());
    }
}
