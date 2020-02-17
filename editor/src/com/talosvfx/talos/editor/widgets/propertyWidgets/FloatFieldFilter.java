package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class FloatFieldFilter implements TextField.TextFieldFilter {

    @Override
    public boolean acceptChar (TextField textField, char c) {
        String text = textField.getText();
        if(text == null) return false;

        int cursorPosition = textField.getCursorPosition();

        if(cursorPosition > text.length()) return false;

        String finalText = text;
        try {
            finalText = text.substring(0, cursorPosition) + c + text.substring(cursorPosition, text.length());
        } catch (Exception e) {
            return false;
        }
        try {
            Float.parseFloat(finalText);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
