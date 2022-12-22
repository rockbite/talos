package com.talosvfx.talos.editor.addons.scene.apps.routines.nodes;

import com.badlogic.gdx.utils.ObjectMap;

public class NumberNode extends AbstractRoutineNode {

    @Override
    public Object getOutputValue(String name, ObjectMap<String, Object> params) {
        return getWidgetValue("value");
    }
}
