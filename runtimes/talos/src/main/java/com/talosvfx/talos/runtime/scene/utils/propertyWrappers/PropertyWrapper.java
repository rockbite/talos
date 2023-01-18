package com.talosvfx.talos.runtime.scene.utils.propertyWrappers;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public abstract class PropertyWrapper<T> implements Cloneable, Json.Serializable {

    public String propertyName;

    public T value;

    public T defaultValue;

    public int index;

    public boolean isValueOverridden = false;

    public void collectAttributes (Array<String> attributes) {
        for (int i = 0; i < attributes.size; i+=2) {
            String type = attributes.get(i);
            if (type.equals("defaultValue")) {
                defaultValue = parseValueFromString(attributes.get(i+1));
            }
        }
    }

    public void setValueUnsafe (Object value) {
        this.value = (T) value;
    }

    public void setValue (T value) {
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }

    @Override
    public PropertyWrapper<T> clone () {
        try {
            PropertyWrapper<T> clone = (PropertyWrapper<T>) super.clone();
            clone.value = value;
            clone.defaultValue = defaultValue;
            clone.propertyName = propertyName;
            clone.index = index;
            clone.isValueOverridden = isValueOverridden;
            return clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
       propertyName = jsonData.getString("propertyName");
       index = jsonData.getInt("index");
       isValueOverridden = jsonData.getBoolean("isValueOverridden", false);
    }

    @Override
    public void write (Json json) {
        json.writeValue("propertyName", propertyName);
        json.writeValue("index", index);
        json.writeValue("isValueOverridden", isValueOverridden);
    }

    public abstract T parseValueFromString (String value);

    public abstract PropertyType getType();

    public void setDefault () {
        this.value = defaultValue;
    }

}
