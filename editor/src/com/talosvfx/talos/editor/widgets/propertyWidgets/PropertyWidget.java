package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.events.PropertyHolderEdited;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.GameObjectContainer;
import com.talosvfx.talos.editor.addons.scene.logic.IPropertyHolder;
import com.talosvfx.talos.editor.addons.scene.logic.components.AComponent;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;
import com.talosvfx.talos.editor.addons.scene.widgets.PropertyPanel;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project2.SharedResources;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.function.Supplier;

public abstract class PropertyWidget<T> extends Table {

	private static final Logger logger = LoggerFactory.getLogger(PropertyWidget.class);

	protected Label propertyName;
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

	public PropertyWidget () {
		build("empty");
	}

	public PropertyWidget (Supplier<T> supplier, ValueChanged<T> valueChanged) {
		this(null, supplier, valueChanged);
	}

	public PropertyWidget (String name, Supplier<T> supplier, ValueChanged<T> valueChanged) {
		this.supplier = supplier;
		this.valueChanged = valueChanged;
		build(name);
	}

	protected void build(String name) {
		if(name != null) {
			hasName = true;
			propertyName = new Label(name + ":", SharedResources.skin);
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
		valueChanged(value);
		PropertyHolderEdited event = Notifications.obtainEvent(PropertyHolderEdited.class);
		event.topLevelPropertiesPanel = topLevelPropertiesPanel;
		event.parentOfPropertyHolder = this.parent;
		boolean isFastChange = isFastChange(this);
		event.fastChange = isFastChange;
		Notifications.fireEvent(event);

		if (topLevelPropertiesPanel != null) {
			topLevelPropertiesPanel.setIgnoringEvents(true);
		}
		//Fire the component update that wont manipulate this properties panel

		if (parent instanceof AComponent) {
			//WE need the context of what scene this shit belongs to
			IPropertyHolder currentHolder = topLevelPropertiesPanel.getCurrentHolder();

			if (currentHolder instanceof GameObjectContainer) {
				GameObject gameObject = ((AComponent)parent).getGameObject();
				SceneUtils.componentUpdated((GameObjectContainer)currentHolder, gameObject, (AComponent)parent, isFastChange);
			} else {
				logger.error("this should be a game object container");
			}
		} else if (parent instanceof AMetadata) {
			if (!isFastChange) {
				AssetRepository.getInstance().saveMetaData((AMetadata)parent, true);
			}
		}

		if (topLevelPropertiesPanel != null) {
			topLevelPropertiesPanel.setIgnoringEvents(false);
		}
	}

	protected boolean isFastChange (PropertyWidget<?> propertyWidget) {
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
			PropertyWidget widget = ClassReflection.newInstance(this.getClass());

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
