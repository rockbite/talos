package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class FloatFieldFilter implements TextField.TextFieldFilter {

    @Override
    public boolean acceptChar (TextField textField, char c) {
        if(Character.isDigit(c)) {
            return true;
        }
        if(c == '.') {
            if(textField.getText() != null && textField.getText().length() > 0) {
                if(textField.getText().contains(".")) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        }

        return false;
    }
}
