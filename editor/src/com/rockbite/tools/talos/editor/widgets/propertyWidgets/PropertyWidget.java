package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.rockbite.tools.talos.TalosMain;

public abstract class PropertyWidget<T> extends Table {

	private Label propertyName;
	protected Table valueContainer;

	protected Property<T> bondedProperty;

	public PropertyWidget () {
		propertyName = new Label("", TalosMain.Instance().getSkin());
		valueContainer = new Table();
		add(propertyName).left();
		propertyName.setAlignment(Align.left);
		add(valueContainer).right().expandX().minWidth(170);

		valueContainer.add(getValueActor()).growX().right();
	}

	public abstract Actor getValueActor();

	public abstract void refresh();

	public void configureForProperty (Property property) {
		this.bondedProperty = property;
		propertyName.setText(property.getPropertyName());
	}
}
