package com.talosvfx.talos.editor.addons.scene.apps.tween.nodes;

import com.badlogic.gdx.utils.ObjectMap;

public class NumberNode extends AbstractTweenNode {

    @Override
    public Object getOutputValue(String name, ObjectMap<String, Object> params) {
        return getWidgetValue("value");
    }
}
