package com.talosvfx.talos.editor.widgets.ui;

import com.kotcrab.vis.ui.widget.VisTextField;

public class FloatRangeDigitFilter implements VisTextField.TextFieldFilter {


	@Override
	public boolean acceptChar (VisTextField textField, char c) {
		boolean hasAPointAlready = false;

		if (textField.getText() == null || textField.getText().isEmpty()) {
			hasAPointAlready = false;
		} else if (textField.getText().contains(".")) {
			hasAPointAlready = true;
		}

		if (Character.isDigit(c)) return true;

		if (c == '.' && hasAPointAlready) return false;

		try {
			float clamped = Float.parseFloat(textField.getText() + c);
		} catch (Exception e) {
			return false;
		}

		return true;
	}
}
