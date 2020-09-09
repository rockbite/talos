package com.talosvfx.talos.editor.addons.shader.nodes;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;

public class InverseNode extends AbstractShaderNode {

    public final String INPUT = "inputValue";

    public final String OUTPUT = "outputValue";

    public InverseNode (Skin skin) {
        super(skin);
    }

    @Override
    public ShaderBuilder.Type getVarType (String name) {

        if (name.equals(OUTPUT)) {
           return  getTargetVarType(INPUT);
        }

        return super.getVarType(name);
    }

    @Override
    public void prepareDeclarations (ShaderBuilder shaderBuilder) {
        String input = getExpression(INPUT, null);

        ShaderBuilder.Type outputType = getVarType(OUTPUT);

        String expression = castTypes("1.0", ShaderBuilder.Type.FLOAT, outputType, CAST_STRATEGY_REPEAT) + " - " + input;

        shaderBuilder.addLine(outputType.getTypeString() + " oneMinus" + getId() + " = " + expression);
    }

    @Override
    public String writeOutputCode (String slotId) {
        return "oneMinus" + getId();
    }
}
