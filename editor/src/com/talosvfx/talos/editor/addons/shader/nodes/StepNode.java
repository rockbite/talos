package com.talosvfx.talos.editor.addons.shader.nodes;

import com.talosvfx.talos.runtime.shaders.ShaderBuilder;

public class StepNode extends AbstractShaderNode {

    public final String INPUT = "inputValue";
    public final String OUTPUT = "outputValue";

    public final String EDGE = "edge";

    @Override
    public ShaderBuilder.Type getVarType (String name) {

        if (name.equals(OUTPUT)) {
            return getTargetVarType(INPUT, ShaderBuilder.Type.FLOAT);
        }

        return super.getVarType(name);
    }


    @Override
    public void prepareDeclarations (ShaderBuilder shaderBuilder) {
        String input = getExpression(INPUT);
        String edge = getExpression(EDGE, null);

        ShaderBuilder.Type outputType = getVarType(OUTPUT);

        String expression = "step(" + edge + ", " + input + ")";

        shaderBuilder.addLine(outputType.getTypeString() + " stepVar" + getId() + " = " + expression);
    }

    @Override
    public String writeOutputCode (String slotId) {
        return "stepVar" + getId();
    }
}
