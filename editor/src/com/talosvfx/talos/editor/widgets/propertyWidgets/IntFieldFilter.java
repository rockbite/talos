package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class IntFieldFilter  implements TextField.TextFieldFilter {

    @Override
    public boolean acceptChar (TextField textField, char c) {
        if(Character.isDigit(c) || c == '-') {
            return true;
        }

        return false;
    }
}
