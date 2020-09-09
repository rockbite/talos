package com.talosvfx.talos.editor.addons.shader.nodes;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.NodeDataModifiedEvent;

public class NumberNode extends AbstractShaderNode {

    public final String OUTPUT = "outputValue";

    public NumberNode (Skin skin) {
        super(skin);
    }

    @Override
    public void prepareDeclarations (ShaderBuilder shaderBuilder) {

    }

    @Override
    public String writeOutputCode (String slotId) {
        return getExpression(OUTPUT, null);
    }
}
