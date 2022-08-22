package com.talosvfx.talos.editor.addons.scene.utils.scriptProperties;


import com.badlogic.gdx.utils.Array;

public abstract class ScriptPropertyWrapper<T> implements Cloneable  {

    public String propertyName;

    private T value;

    public T defaultValue;


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
    public ScriptPropertyWrapper<T> clone () {
        try {
            ScriptPropertyWrapper<T> clone = (ScriptPropertyWrapper<T>) super.clone();
            clone.value = value;
            clone.defaultValue = defaultValue;
            clone.propertyName = propertyName;
            return clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public abstract T parseValueFromString (String value);

    public void setDefault () {
        this.value = defaultValue;
    }
}
