package com.talosvfx.talos.editor.addons.shader.nodes;

import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;

public class VectorToXYNode extends AbstractShaderNode {

    public final String X = "X";
    public final String Y = "Y";

    public final String INPUT = "inputValue";

    @Override
    public void prepareDeclarations (ShaderBuilder shaderBuilder) {
        String inputVal = getExpression(INPUT, null);

        String x = "(" + inputVal + ").x";
        String y = "(" + inputVal + ").y";

        shaderBuilder.declareVariable(ShaderBuilder.Type.FLOAT, "vec2OutX" + getId(), x);
        shaderBuilder.declareVariable(ShaderBuilder.Type.FLOAT, "vec2OutY" + getId(), y);
    }

    @Override
    public String writeOutputCode (String slotId) {
        if (slotId.equals(X)) {
            return "vec2OutX" + getId();
        } else if (slotId.equals(Y)) {
            return "vec2OutY" + getId();
        }

        return "";
    }

    @Override
    protected void inputStateChanged (boolean isInputDynamic) {

    }

    @Override
    protected boolean isInputDynamic () {
        return false;
    }
}
