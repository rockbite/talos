package com.talosvfx.talos.editor.addons.scene.utils.scriptProperties;

import com.badlogic.gdx.utils.Array;

public class ScriptPropertyFloatWrapper extends ScriptPropertyWrapper<Float> {

    float defaultValue;
    float minValue;
    float maxValue;

    @Override
    public String getTypeName () {
        return "float";
    }

    @Override
    public void collectAttributes (Array<String> attributes) {
        // TODO: 8/18/2022 do this with annotations
    }

    @Override
    public ScriptPropertyWrapper<Float> copy () {
        ScriptPropertyFloatWrapper scriptPropertyFloatWrapper = new ScriptPropertyFloatWrapper();
        scriptPropertyFloatWrapper.propertyName = this.propertyName;
        scriptPropertyFloatWrapper.minValue = this.minValue;
        scriptPropertyFloatWrapper.maxValue = this.maxValue;
        scriptPropertyFloatWrapper.defaultValue = this.defaultValue;

        return scriptPropertyFloatWrapper;
    }
}
