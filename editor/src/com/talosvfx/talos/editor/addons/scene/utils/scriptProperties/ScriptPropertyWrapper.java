package com.talosvfx.talos.editor.addons.scene.utils.scriptProperties;


import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public abstract class ScriptPropertyWrapper<T> {

    public String propertyName;

    public T value;
    public T defaultValue;

    public abstract String getTypeName();

    public abstract void collectAttributes (Array<String> attributes);

    public abstract ScriptPropertyWrapper<T> copy();
}
