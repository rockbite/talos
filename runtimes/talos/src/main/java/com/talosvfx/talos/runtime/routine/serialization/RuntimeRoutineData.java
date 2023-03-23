package com.talosvfx.talos.runtime.routine.serialization;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.routine.RoutineInstance;
import com.talosvfx.talos.runtime.routine.misc.PropertyTypeWrapperMapper;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyType;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;
import lombok.Data;

@Data
public class RuntimeRoutineData implements BaseRoutineData, Json.Serializable {

	private transient RoutineInstance routineInstance;

	private int exposedPropertyIndex = 0;

	private Array<PropertyWrapper<?>> propertyWrappers = new Array<>();

	private transient boolean canWrite;

	private JsonValue jsonNodes;
	private JsonValue jsonConnections;
	private JsonValue jsonGroups;

	@Override
	public void write (Json json) {

	}

	@Override
	public void read (Json json, JsonValue root) {
		jsonNodes = root.get("list");
		jsonConnections = root.get("connections");
		jsonGroups = root.get("groups");

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
		routineInstance = createInstance(false);
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
			PropertyWrapper<?> propertyWrapper = PropertyTypeWrapperMapper.getWrapperForPropertyType(type).getConstructor().newInstance();
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

	@Override
	public JsonValue getJsonNodes () {
		return jsonNodes;
	}

	@Override
	public JsonValue getJsonConnections () {
		return jsonConnections;
	}

	public RoutineInstance createInstance (boolean external) {
		RoutineInstance routine = new RoutineInstance();
		if (external && canWrite) {
			Json json = new Json();
			String jsonData = json.prettyPrint(this);
			JsonReader jsonReader = new JsonReader();
			JsonValue parse = jsonReader.parse(jsonData);
			read(json, parse);
		}
		routine.loadFrom(this, RuntimeContext.getInstance().configData.getRoutineConfigMap());
		return routine;
	}
}
