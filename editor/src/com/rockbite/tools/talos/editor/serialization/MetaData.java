package com.rockbite.tools.talos.editor.serialization;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class MetaData implements Json.Serializable {

    @Override
    public void write(Json json) {
        json.writeArrayStart("scopeDefaults");
        for(int i = 0; i < 10; i++) {
            NumericalValue val = TalosMain.Instance().globalScope.getDynamicValue(i);
            float[] arr = new float[4];
            for(int j = 0; j < 4; j++) {
                arr[j] = val.get(j);
            }
            json.writeValue(arr);
        }
        json.writeArrayEnd();

    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        JsonValue arr = jsonData.get("scopeDefaults");
        int iter = 0;
        for(JsonValue item: arr) {
            NumericalValue val = new NumericalValue();
            val.set(item.get(0).asFloat(), item.get(1).asFloat(), item.get(2).asFloat(), item.get(3).asFloat());
            TalosMain.Instance().globalScope.setDynamicValue(iter++, val);
        }
    }
}
