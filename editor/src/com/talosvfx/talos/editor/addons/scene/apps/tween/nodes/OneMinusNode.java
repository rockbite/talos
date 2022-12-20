package com.talosvfx.talos.editor.addons.scene.apps.tween.nodes;

import com.badlogic.gdx.utils.ObjectMap;

public class OneMinusNode extends AbstractRoutineNode {

    @Override
    public Object getOutputValue(String name, ObjectMap<String, Object> params) {

        float input = getWidgetFloatValue("input");

        return input * -1;

    }
}
