package com.talosvfx.talos.editor.addons.shader.nodes;

import com.talosvfx.talos.runtime.vfx.shaders.ShaderBuilder;

public class Vector2Node extends AbstractShaderNode {

    public final String X = "X";
    public final String Y = "Y";

    public final String OUTPUT = "outputValue";

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
        return "vec4 outputVal = vec4(" + output + ".x, " + output + ".y, 0.0, 1.0); return outputVal;";
    }
}
