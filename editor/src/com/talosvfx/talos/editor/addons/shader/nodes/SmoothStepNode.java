package com.talosvfx.talos.editor.addons.shader.nodes;


import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;

public class SmoothStepNode extends AbstractShaderNode {

    public final String INPUT = "inputValue";
    public final String OUTPUT = "outputValue";

    public final String EDGE_ONE = "edgeOne";
    public final String EDGE_TWO = "edgeTwo";

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
        String edgeOne = getExpression(EDGE_ONE, null);
        String edgeTwo = getExpression(EDGE_TWO, null);

        ShaderBuilder.Type outputType = getVarType(OUTPUT);

        String expression = "smoothstep(" + edgeOne + ", " + edgeTwo + ", " + input + ")";

        shaderBuilder.addLine(outputType.getTypeString() + " smoothStepVar" + getId() + " = " + expression);
    }

    @Override
    public String writeOutputCode (String slotId) {
        return "smoothStepVar" + getId();
    }
}
