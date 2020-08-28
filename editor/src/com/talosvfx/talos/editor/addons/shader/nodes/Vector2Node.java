package com.talosvfx.talos.editor.addons.shader.nodes;

import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;

public class Vector2Node extends AbstractShaderNode {

    public final String X = "X";
    public final String Y = "Y";

    public final String OUTPUT = "outputValue";

    @Override
    public void prepareDeclarations (ShaderBuilder shaderBuilder) {

    }

    @Override
    public String writeOutputCode (String slotId) {
        String xVal = getExpression(X, null);
        String yVal = getExpression(Y, null);

        String vec = "(vec2(" + xVal + ", " + yVal + "))";

        return vec;
    }
}
