package com.talosvfx.talos.editor.addons.shader.nodes;

import com.badlogic.gdx.utils.Align;
import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;
import com.talosvfx.talos.editor.addons.shader.widgets.ShaderBox;
import com.talosvfx.talos.editor.nodes.NodeWidget;

public class MixNode extends AbstractShaderNode {

    public final int INPUT_A = 0;
    public final int INPUT_B = 1;

    public final int OUTPUT = 0;

    @Override
    public void prepareDeclarations(ShaderBuilder shaderBuilder) {

    }

    @Override
    public String writeOutputCode(String slotId) {
        return null;
    }
/*
    @Override
    protected void configureConnections () {

        addConnection("val A", INPUT_A, Align.left);
        addConnection("val B", INPUT_B, Align.left);

        addConnection("output", OUTPUT, Align.right);

        shaderBox = new ShaderBox();
        dynamicContentTable.add(shaderBox).padTop(60).growX().expand().height(150).padLeft(-17).padRight(-16);
    }*/
/*
    @Override
    public void prepareDeclarations (ShaderBuilder shaderBuilder) {

        String exprA = inputStrings.get(INPUT_A);
        String exprB = inputStrings.get(INPUT_B);

        if(exprA != null && exprB != null) {
            shaderBuilder.addLine("vec4 mixVar" + getId() + " = mix(" + exprA + "," + exprB + ", 0.5)");
        } else {
            shaderBuilder.addLine("vec4 mixVar" + getId() + " = vec4(0.0)");
        }

    }

    @Override
    public String writeOutputCode (int slotId) {
        return "mixVar" + getId();
    }
    */
}
