package com.talosvfx.talos.editor.addons.scene.apps.tween.nodes;

public class OneMinusNode extends AbstractTweenNode {

    @Override
    public Object getOutputValue(String name) {

        float input = getWidgetFloatValue("input");

        return input * -1;

    }
}
