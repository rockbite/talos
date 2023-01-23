package com.talosvfx.talos.editor.addons.shader.nodes;

import com.talosvfx.talos.runtime.vfx.shaders.ShaderBuilder;

public class Vector3Node extends AbstractShaderNode {

    public final String X = "X";
    public final String Y = "Y";
    public final String Z = "Z";

    public final String OUTPUT = "outputValue";

    @Override
    public void prepareDeclarations (ShaderBuilder shaderBuilder) {
        String xVal = getExpression(X, null);
        String yVal = getExpression(Y, null);
        String zVal = getExpression(Z, null);

        String vec = "vec3(" + xVal + ", " + yVal + ", " + zVal + ")";
        shaderBuilder.declareVariable(ShaderBuilder.Type.VEC3, "vec3Val" + getId(), vec);

    }

    @Override
    public String writeOutputCode (String slotId) {
        return "vec3Val" + getId();
    }

    @Override
    protected String getPreviewLine (String expression) {
        String output = "vec3Val" + getId();
        return "vec4 outputVal = vec4(" + output + ".x, " + output + ".y, " + output + ".z, 1.0); return outputVal;";
    }
}
