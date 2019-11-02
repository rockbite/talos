package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.rockbite.tools.talos.TalosMain;

public class GlobalValueRowWidget extends ListCustomRow {

	private GlobalValueWidget parentWidget;

	int index;

	Label identifier;
	GlobalValueRowWidgetType type = GlobalValueRowWidgetType.BONE;
	Button typeToggleButton;

	// BONE TYPE WIDGETS
	SelectBox<String> boneNames;
	CheckBox boneTypeChange;

	// STATIC TYPE WIDGETS
	TextField first;
	TextField second;
	TextField third;

	public GlobalValueRowWidget (GlobalValueWidget parentWidget, int index) {
		this.parentWidget = parentWidget;

		Skin skin = TalosMain.Instance().getSkin();
		identifier = new Label(String.valueOf(index), skin);
		typeToggleButton = new Button(skin);

		boneNames = new SelectBox<>(skin);
		boneTypeChange = new CheckBox("", skin);

		first = new TextField("0", skin);
		second = new TextField("0", skin);
		third = new TextField("0", skin);

		reconstruct();
	}

	public int getIndex () {
		return index;
	}

	private void reconstruct () {
		clearChildren();
		switch (type) {
			case BONE:
				add(identifier);
				add(boneNames).expandX();
				add(boneTypeChange);
				add(typeToggleButton);
				break;
			case STATIC:
				break;
		}
	}

	public enum GlobalValueRowWidgetType {
		BONE, STATIC
	}
}
