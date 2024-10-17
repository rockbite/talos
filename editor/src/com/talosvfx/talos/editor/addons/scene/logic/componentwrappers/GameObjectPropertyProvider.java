package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectActiveChanged;
import com.talosvfx.talos.editor.addons.scene.events.commands.GONameChangeCommand;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.widgets.propertyWidgets.EditableLabelWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.LabelWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import com.talosvfx.talos.runtime.scene.GameObject;

import com.talosvfx.talos.runtime.utils.Supplier;

public class GameObjectPropertyProvider implements IPropertyProvider {

	private final GameObject gameObject;

	public GameObjectPropertyProvider (GameObject gameObject) {
		this.gameObject = gameObject;
	}

	@Override
	public Array<PropertyWidget> getListOfProperties () {
		Array<PropertyWidget> properties = new Array<>();

		LabelWidget uuidWidget = new LabelWidget("UUID", new Supplier<String>() {
			@Override
			public String get () {
				return gameObject.uuid.toString();
			}
		}, gameObject);

		EditableLabelWidget labelWidget = new EditableLabelWidget("Name", new Supplier<String>() {
			@Override
			public String get () {
				return gameObject.getName();
			}
		}, new PropertyWidget.ValueChanged<String>() {
			@Override
			public void report (String value) {
				GONameChangeCommand command = Notifications.obtainEvent(GONameChangeCommand.class).set(gameObject.getGameObjectContainerRoot(), gameObject, value);
				Notifications.fireEvent(command);
			}
		}, gameObject);

		properties.add(labelWidget);
		properties.add(uuidWidget);

		PropertyWidget activeWidget = WidgetFactory.generate(gameObject, "active", "Active");
		activeWidget.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				GameObjectActiveChanged activeChanged = Notifications.obtainEvent(GameObjectActiveChanged.class);
				activeChanged.target = gameObject;
				Notifications.fireEvent(activeChanged);
			}
		});
		properties.add(activeWidget);

		PropertyWidget optimizeWidget = WidgetFactory.generate(gameObject, "optimizeSkeletonBones", "Optimize bones");
		properties.add(optimizeWidget);

		return properties;
	}

	@Override
	public String getPropertyBoxTitle () {
		return "Game Object";
	}

	@Override
	public int getPriority () {
		return 0;
	}

	@Override
	public Class<? extends IPropertyProvider> getType () {
		return getClass();
	}
}
