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
        String exprA = inputStrings.get(INPUT_A);
        String exprB = inputStrings.get(INPUT_B);

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

        if(exprA == null) {
            exprA = "vec4(0.0)";
        }
        if(exprB == null) {
            exprB = "vec4(0.0)";
        }

        String expression = "(" + exprA + ") " + operand + " (" + exprB + ")";

        if (clamp) {
            expression = "clamp(" + expression +  ", 0.0, 1.0)";
        }

        shaderBuilder.addLine("vec4 sumVar" + getId() + " = " + expression);
    }

    @Override
    public String writeOutputCode(String slotId) {
        return "sumVar" + getId();
    }
}
