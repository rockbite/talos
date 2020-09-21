package com.talosvfx.talos.editor.addons.shader.nodes;

import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;

public class MathNode extends AbstractShaderNode {

    public final String INPUT_A = "valueA";
    public final String INPUT_B = "valueB";

    public final String OPERATION = "operation";
    public final String CLAMP = "clamp";

    public final String OUTPUT = "outputValue";

    @Override
    public ShaderBuilder.Type getVarType (String name) {

        if (name.equals(OUTPUT)) {
            ShaderBuilder.Type maxType = getMaxType(
                    getTargetVarType(INPUT_A, ShaderBuilder.Type.FLOAT),
                    getTargetVarType(INPUT_B, ShaderBuilder.Type.FLOAT));

            return maxType;
        }

        return super.getVarType(name);
    }

    @Override
    public void prepareDeclarations(ShaderBuilder shaderBuilder) {
        String exprA = getExpression(INPUT_A, null);
        String exprB = getExpression(INPUT_B, null);

        ShaderBuilder.Type outputType = getVarType(OUTPUT);

        if(outputType != getTargetVarType(INPUT_A, ShaderBuilder.Type.FLOAT) || outputType != getTargetVarType(INPUT_B, ShaderBuilder.Type.FLOAT)) {
            // gotta cast
            exprA = castTypes(exprA, getTargetVarType(INPUT_A, ShaderBuilder.Type.FLOAT), outputType, CAST_STRATEGY_REPEAT);
            exprB = castTypes(exprB, getTargetVarType(INPUT_B, ShaderBuilder.Type.FLOAT), outputType, CAST_STRATEGY_REPEAT);
        }

        boolean clamp = (boolean) widgetMap.get(CLAMP).getValue();

        String operation = (String) widgetMap.get(OPERATION).getValue();
        String operand = "+";

        String expression = "";

        if (operation.equals("SIN")) {
            expression = "sin(" + exprA + ") * (" + exprB + ")";
        } else if (operation.equals("COS")) {
            expression = "cos(" + exprA + ") * (" + exprB + ")";
        } else if(operation.equals("POW")) {
            expression = "pow(" + exprA + ", " + exprB + ")";
        } else {
            if (operation.equals("ADD")) {
                operand = "+";
            } else if (operation.equals("SUB")) {
                operand = "-";
            } else if (operation.equals("MUL")) {
                operand = "*";
            } else if (operation.equals("DIV")) {
                operand = "/";
            }

            expression = "(" + exprA + ") " + operand + " (" + exprB + ")";
        }


        if (clamp) {
            expression = "fract(" + expression +  ")";
        }

        shaderBuilder.addLine(outputType.getTypeString() + " mathVar" + getId() + " = " + expression);
    }

    @Override
    public String writeOutputCode(String slotId) {
        return "mathVar" + getId();
    }
}
