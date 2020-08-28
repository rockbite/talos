package com.talosvfx.talos.editor.addons.shader.nodes;

import com.badlogic.gdx.utils.Align;
import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;
import com.talosvfx.talos.editor.addons.shader.widgets.ShaderBox;
import com.talosvfx.talos.editor.nodes.NodeWidget;

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
