package com.talosvfx.talos.editor.addons.scene.utils.scriptProperties;


import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public abstract class ScriptPropertyWrapper<T> {

    T value;

    public abstract String getTypeName();

    public abstract void collectAttributes (Array<String> attributes);
}
