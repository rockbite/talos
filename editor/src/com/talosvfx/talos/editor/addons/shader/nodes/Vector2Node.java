package com.talosvfx.talos.editor.addons.shader.nodes;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.NodeDataModifiedEvent;

public class Vector2Node extends AbstractShaderNode {

    public final String X = "X";
    public final String Y = "Y";

    public final String OUTPUT = "outputValue";

    public Vector2Node (Skin skin) {
        super(skin);
    }

    @Override
    public void prepareDeclarations (ShaderBuilder shaderBuilder) {
        String xVal = getExpression(X, null);
        String yVal = getExpression(Y, null);

        String vec = "vec2(" + xVal + ", " + yVal + ")";
        shaderBuilder.declareVariable(ShaderBuilder.Type.VEC2, "vec2Val" + getId(), vec);

    }

    @Override
    public String writeOutputCode (String slotId) {
        return "vec2Val" + getId();
    }

    @Override
    protected String getPreviewLine (String expression) {
        String output = "vec2Val" + getId();
        return "gl_FragColor = vec4(" + output + ".x, " + output + ".y, 0.0, 1.0)";
    }
}
