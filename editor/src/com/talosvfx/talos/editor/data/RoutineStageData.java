package com.talosvfx.talos.editor.data;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.RoutineExposedVariableNodeWidget;
import com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types.PropertyTypeWidgetMapper;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.GameResourceOwner;
import com.talosvfx.talos.runtime.routine.RoutineInstance;
import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.runtime.routine.serialization.BaseRoutineData;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyType;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class RoutineStageData extends DynamicNodeStageData implements BaseRoutineData {

	@Getter@Setter
	protected transient String name;

	private transient RoutineInstance routineInstance;

	private int exposedPropertyIndex = 0;

	private Array<PropertyWrapper<?>> propertyWrappers = new Array<>();

	private transient boolean canWrite;
	private String talosIdentifier;

	@Override
	public void read (Json json, JsonValue root) {
		super.read(json, root);
		talosIdentifier = GameResourceOwner.readTalosIdentifier(root);
		propertyWrappers.clear();
		JsonValue propertiesJson = root.get("propertyWrappers");
		if (propertiesJson != null) {
			for (JsonValue propertyJson : propertiesJson) {
				String className = propertyJson.getString("className", "");
				JsonValue property = propertyJson.get("property");
				if (property != null) {
					try {
						Class clazz = ClassReflection.forName(className);
						PropertyWrapper propertyWrapper = (PropertyWrapper) ClassReflection.newInstance(clazz);
						propertyWrapper.read(json, property);
						propertyWrappers.add(propertyWrapper);
					} catch (ReflectionException e) {
						e.printStackTrace();
					}
				}
			}
		}

		exposedPropertyIndex = root.getInt("propertyWrapperIndex", 0);
		//Construct the routine instance from data
		routineInstance = createInstance(false, talosIdentifier);
	}

	@Override
	public <T extends DynamicNodeStageData> void constructForUI (DynamicNodeStage<T> dynamicNodeStage) {
		super.constructForUI(dynamicNodeStage);

		canWrite = true;
		for (NodeWidget node : nodes) {
			if (node instanceof RoutineExposedVariableNodeWidget) {
				((RoutineExposedVariableNodeWidget) node).update(getPropertyWrapperWithIndex(((RoutineExposedVariableNodeWidget) node).index));
			}
		}
	}

	@Override
	public void write (Json json) {
		super.write(json);
		json.writeValue("propertyWrapperIndex", exposedPropertyIndex);

		json.writeObjectStart("propertyWrappers");
		for (PropertyWrapper<?> propertyWrapper : propertyWrappers) {
			json.writeObjectStart("property");
			json.writeValue("className", propertyWrapper.getClass().getName());
			json.writeValue("property", propertyWrapper);
			json.writeObjectEnd();
		}
		json.writeObjectEnd();

	}

	public PropertyWrapper<?> createNewPropertyWrapper (PropertyType propertyType) {
		PropertyWrapper<?> propertyWrapper = createPropertyInstanceOfType(propertyType);
		propertyWrapper.index = exposedPropertyIndex;
		exposedPropertyIndex++;
		propertyWrappers.add(propertyWrapper);
		return propertyWrapper;
	}

	private PropertyWrapper<?> createPropertyInstanceOfType (PropertyType type) {
		try {
			PropertyWrapper<?> propertyWrapper = PropertyTypeWidgetMapper.getWrapperForPropertyType(type).getConstructor().newInstance();
			return propertyWrapper;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public PropertyWrapper<?> getPropertyWrapperWithIndex (int index) {
		for (PropertyWrapper<?> propertyWrapper : propertyWrappers) {
			if (propertyWrapper.index == index) {
				return propertyWrapper;
			}
		}

		return null;
	}

	public void removeExposedVariablesWithIndex (int index) {
		PropertyWrapper<?> propertyWrapperWithIndex = getPropertyWrapperWithIndex(index);
		propertyWrappers.removeValue(propertyWrapperWithIndex, true);

		for (NodeWidget node : nodes) {
			if (node instanceof RoutineExposedVariableNodeWidget) {
				RoutineExposedVariableNodeWidget widget = ((RoutineExposedVariableNodeWidget) node);
				if (widget.index == index) {
					widget.update(null);
				}
			}
		}
	}

	public void changeExposedVariableKey (int index, String newKey) {
		PropertyWrapper<?> propertyWrapper = getPropertyWrapperWithIndex(index);
		propertyWrapper.propertyName = newKey;
		for (NodeWidget node : nodes) {
			if (node instanceof RoutineExposedVariableNodeWidget) {
				RoutineExposedVariableNodeWidget widget = ((RoutineExposedVariableNodeWidget) node);
				if (widget.index == index) {
					widget.update(routineInstance.getPropertyWrapperWithIndex(index));
				}
			}
		}
	}

	public RoutineInstance createInstance (boolean external, String talosIdentifier) {
		RoutineInstance routine = new RoutineInstance();
		routine.setTalosIdentifier(talosIdentifier);
		if (external && canWrite) {
			Json json = new Json();
			String jsonData = json.prettyPrint(this);
			JsonReader jsonReader = new JsonReader();
			JsonValue parse = jsonReader.parse(jsonData);
			parse.addChild("talosIdentifier", new JsonValue(talosIdentifier));
			read(json, parse);
		}
		routine.loadFrom(this, RuntimeContext.getInstance().getTalosContext(talosIdentifier).getConfigData().getRoutineConfigMap());
		return routine;
	}
}
