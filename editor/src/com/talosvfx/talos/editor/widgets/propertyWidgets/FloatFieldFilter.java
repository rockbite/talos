package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class FloatFieldFilter implements TextField.TextFieldFilter {

    @Override
    public boolean acceptChar (TextField textField, char c) {
        String text = textField.getText();
        int cursorPosition = textField.getCursorPosition();
        String finalText = text.substring(0, cursorPosition) + c + text.substring(cursorPosition, text.length());
        try {
            Float.parseFloat(finalText);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
