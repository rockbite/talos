package com.talosvfx.talos.editor.addons.shader.nodes;


import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;

public class MathNode extends AbstractShaderNode {

    public final String INPUT_A = "valueA";
    public final String INPUT_B = "valueB";

    public final String OPERATION = "operation";
    public final String CLAMP = "clamp";

    public final String OUTPUT = "outputValue";

    @Override
    public void prepareDeclarations(ShaderBuilder shaderBuilder) {
        String exprA = getExpression(INPUT_A, null);
        String exprB = getExpression(INPUT_B, null);

        boolean clamp = (boolean) widgetMap.get(CLAMP).getValue();

        String operation = (String) widgetMap.get(OPERATION).getValue();
        String operand = "+";

        if (operation.equals("ADD")) {
            operand = "+";
        } else if (operation.equals("SUB")) {
            operand = "-";
        } else if (operation.equals("MUL")) {
            operand = "*";
        } else if (operation.equals("DIV")) {
            operand = "/";
        }

        String expression = "(" + exprA + ") " + operand + " (" + exprB + ")";

        if (clamp) {
            expression = "fract(" + expression +  ")";
        }

        expression = "vec4(" + expression + ")";

        shaderBuilder.addLine("vec4 sumVar" + getId() + " = " + expression);
    }

    @Override
    public String writeOutputCode(String slotId) {
        return "sumVar" + getId();
    }
}
