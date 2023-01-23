package com.talosvfx.talos.editor.addons.shader.nodes;

import com.talosvfx.talos.runtime.vfx.shaders.ShaderBuilder;

public class VertexColorNode extends AbstractShaderNode {

    public final String OUTPUT = "outputValue";

    @Override
    protected void inputStateChanged (boolean isInputDynamic) {

    }

    @Override
    protected boolean isInputDynamic () {
        return false;
    }

    @Override
    public void prepareDeclarations (ShaderBuilder shaderBuilder) {

    }

    @Override
    public String writeOutputCode (String slotId) {
        return "v_color";
    }
}
