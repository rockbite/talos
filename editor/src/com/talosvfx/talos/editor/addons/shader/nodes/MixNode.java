package com.talosvfx.talos.editor.addons.shader.nodes;

import com.talosvfx.talos.runtime.vfx.shaders.ShaderBuilder;

public class MixNode extends AbstractShaderNode {

    public final String INPUT_A = "valueA";
    public final String INPUT_B = "valueB";

    public final String FRACTION = "frac";

    public final String OUTPUT = "outputValue";

    @Override
    public void prepareDeclarations(ShaderBuilder shaderBuilder) {
        String exprA = inputStrings.get(INPUT_A);
        String exprB = inputStrings.get(INPUT_B);

        if(exprA == null) {
            exprA = "vec4(0.0)";
        }
        if(exprB == null) {
            exprB = "vec4(0.0)";
        }

        shaderBuilder.addLine("vec4 mixVar" + getId() + " = mix(" + exprA + "," + exprB + ", " + widgetMap.get(FRACTION).getValue() + ")");
    }

    @Override
    public String writeOutputCode(String slotId) {
        return "mixVar" + getId();
    }

}
