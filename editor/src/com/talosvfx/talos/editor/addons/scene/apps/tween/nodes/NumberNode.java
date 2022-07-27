package com.talosvfx.talos.editor.addons.scene.apps.tween.nodes;

public class NumberNode extends AbstractTweenNode {

    @Override
    public Object getOutputValue(String name) {
        return getWidgetValue("value");
    }
}
