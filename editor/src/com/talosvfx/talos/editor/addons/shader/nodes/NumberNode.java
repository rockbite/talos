package com.talosvfx.talos.editor.addons.shader.nodes;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.NodeDataModifiedEvent;

public class NumberNode extends AbstractShaderNode {

    public final String OUTPUT = "outputValue";

    @Override
    public void prepareDeclarations (ShaderBuilder shaderBuilder) {
        widgetMap.get(OUTPUT).addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent changeEvent, Actor actor) {
                Notifications.fireEvent(Notifications.obtainEvent(NodeDataModifiedEvent.class).set(NumberNode.this));
            }
        });
    }

    @Override
    public String writeOutputCode (String slotId) {
        return getExpression(OUTPUT, null);
    }
}
