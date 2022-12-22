package com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers;


import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import lombok.Getter;
import lombok.Setter;

public abstract class PropertyWrapper<T> implements Cloneable, Json.Serializable {

    public String propertyName;

    public T value;

    public T defaultValue;

    public int index;

    @Getter @Setter
    private transient PropertyType type;

    public void collectAttributes (Array<String> attributes) {
        for (int i = 0; i < attributes.size; i+=2) {
            String type = attributes.get(i);
            if (type.equals("defaultValue")) {
                defaultValue = parseValueFromString(attributes.get(i+1));
            }
        }
    };

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
    }

    @Override
    public void write (Json json) {
        json.writeValue("propertyName", propertyName);
        json.writeValue("index", index);
    }

    public abstract T parseValueFromString (String value);

    public void setDefault () {
        this.value = defaultValue;
    }
}
