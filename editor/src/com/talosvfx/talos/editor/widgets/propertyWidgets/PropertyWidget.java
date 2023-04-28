package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Constructor;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.events.PropertyHolderEdited;
import com.talosvfx.talos.editor.addons.scene.logic.MultiPropertyHolder;
import com.talosvfx.talos.editor.addons.scene.logic.componentwrappers.GameObjectPropertyHolder;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectContainer;
import com.talosvfx.talos.editor.addons.scene.logic.IPropertyHolder;
import com.talosvfx.talos.runtime.assets.AMetadata;
import com.talosvfx.talos.editor.addons.scene.widgets.PropertyPanel;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.runtime.scene.components.AComponent;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.LabelWithZoom;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.function.Supplier;

public abstract class PropertyWidget<T> extends Table {

	private static final Logger logger = LoggerFactory.getLogger(PropertyWidget.class);

	@Getter
	protected LabelWithZoom propertyName;
	protected Table valueContainer;
	protected T value;

	ChangeListener listener;

	private Supplier<T> supplier;
	private ValueChanged<T> valueChanged;

	private Object parent;

	private boolean hasName = true;

	@Setter
	protected PropertyPanel topLevelPropertiesPanel;

	@Setter
	public ChangeListener injectedChangeListener;

	public void toggleHide (boolean hidden) {
		//Check if we are in a cell
		if (getParent() instanceof Table) {
			Cell<PropertyWidget<T>> cell = ((Table)getParent()).getCell(this);
			if (cell != null) {
				if (hidden) {
					setVisible(false);
					cell.height(0);
					((Table)getParent()).invalidateHierarchy();
				} else {
					setVisible(true);
					cell.height(Value.prefHeight);
					((Table)getParent()).invalidateHierarchy();
				}
			}
		}
	}

	public void setParent (Object scriptProperty) {
		this.parent = scriptProperty;
	}

	public Object getParentObject () {
		return parent;
	}

	public interface ValueChanged<T> {
		void report(T value);
	}

	protected PropertyWidget () {

	}

	public PropertyWidget (Supplier<T> supplier, ValueChanged<T> valueChanged, Object parent) {
		this("empty", supplier, valueChanged, parent);
	}

	public PropertyWidget (String name, Supplier<T> supplier, ValueChanged<T> valueChanged, Object parent) {
		this.supplier = supplier;
		this.valueChanged = valueChanged;
		setParent(parent);
		build(name);
	}

	protected void build(String name) {
		if(name != null) {
			hasName = true;
			propertyName = new LabelWithZoom(name + ":", SharedResources.skin);
			propertyName.setAlignment(Align.left);
			valueContainer = new Table();

			if (isFullSize()) {
				add(propertyName).left().growX();
				row();
				add(valueContainer).growX();
			} else {
				add(propertyName).left();
				add(valueContainer).right().expandX().minWidth(170);
			}

			addToContainer(getSubWidget());
		} else {
			hasName = false;
			add(getSubWidget()).growX();
		}
	}

	protected void addToContainer(Actor actor) {
		valueContainer.add(actor).growX().width(0).right();
	}

	public T getValue() {
		return supplier.get();
	}

	public Actor getSubWidget() {
		return null;
	}

	public void updateValue() {
		this.value = supplier.get();
		updateWidget(value);
	}

	public abstract void updateWidget(T value);


	protected void callValueChanged (T value) {
		boolean isFastChange = isFastChange();
		callValueChanged(value, isFastChange);
	}

	protected void callValueChanged (T value, boolean isFastChange) {
		valueChanged(value);
		PropertyHolderEdited event = Notifications.obtainEvent(PropertyHolderEdited.class);
		event.topLevelPropertiesPanel = topLevelPropertiesPanel;
		event.parentOfPropertyHolder = this.parent;
		event.fastChange = isFastChange;
		Notifications.fireEvent(event);

		if (topLevelPropertiesPanel != null) {
			topLevelPropertiesPanel.setIgnoringEvents(true);
		}
		//Fire the component update that wont manipulate this properties panel

		if (parent instanceof AComponent) {
			//WE need the context of what scene this shit belongs to
			IPropertyHolder currentHolder = topLevelPropertiesPanel.getCurrentHolder();

			if (currentHolder instanceof GameObjectPropertyHolder) {
				GameObject gameObject = ((AComponent)parent).getGameObject();
				SceneUtils.componentUpdated(gameObject.getGameObjectContainerRoot(), gameObject, (AComponent)parent, isFastChange);
			} else {
				logger.error("this should be a game object container");
			}
		} else if (parent instanceof AMetadata) {
			if (!isFastChange) {
				AssetRepository.getInstance().saveMetaData((AMetadata)parent, true);
			}
		} else if (parent instanceof GameObjectContainer) {
			if (!isFastChange) {
				SceneUtils.markContainerChanged((GameObjectContainer)parent);
			}
		} else if (parent instanceof MultiPropertyHolder.MultiPropertyProvider) {
			MultiPropertyHolder.MultiPropertyProvider multiPropertyProvider = (MultiPropertyHolder.MultiPropertyProvider) parent;
			Class<?> type = multiPropertyProvider.getWidgetClass(this);

			multiPropertyProvider.propagateChanges();

			if (AComponent.class.isAssignableFrom(type)) {
				Array<PropertyWidget> propertyWidgets = multiPropertyProvider.getPropertyWidgetsFor(this);
				for (PropertyWidget propertyWidget : propertyWidgets) {
					AComponent component = ((AComponent) propertyWidget.getParentObject());
					SceneUtils.fireComponentUpdateEvent(component, isFastChange);
				}
				GameObjectContainer container = ((AComponent) propertyWidgets.first().getParentObject()).getGameObject().getGameObjectContainerRoot();

				if (!isFastChange) {
					SceneUtils.markContainerChanged(container);
				}
			}
		}

		if (topLevelPropertiesPanel != null) {
			topLevelPropertiesPanel.setIgnoringEvents(false);
		}
	}

	public boolean isFastChange () {
		return false;
	}

	public void valueChanged(T value) {
		fire(new ChangeListener.ChangeEvent());
		valueChanged.report(value);
		if (injectedChangeListener != null) {
			injectedChangeListener.changed(new ChangeListener.ChangeEvent(), this);
		}
	}

	protected boolean isFullSize() {
		return !hasName;
	}

	public PropertyWidget clone()  {
		try {
			Constructor constructor = ClassReflection.getDeclaredConstructor(this.getClass());
			constructor.setAccessible(true);
			PropertyWidget widget = (PropertyWidget) constructor.newInstance();
			widget.build("empty");

			widget.supplier = this.supplier;
			widget.valueChanged = this.valueChanged;
			if (widget.propertyName != null && this.propertyName != null) {
				widget.propertyName.setText(this.propertyName.getText());
			}
			return widget;


		} catch (ReflectionException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void set(Supplier<T> supplier, ValueChanged<T> valueChanged) {
		this.supplier = supplier;
		this.valueChanged = valueChanged;
	}

	public void report(T value) {
		valueChanged.report(value);
	}
}
