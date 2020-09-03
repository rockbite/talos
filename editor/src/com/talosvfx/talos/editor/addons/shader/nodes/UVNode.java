package com.talosvfx.talos.editor.addons.shader.nodes;

import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;

public class UVNode extends AbstractShaderNode {

    public final String OUTPUT_UV = "outputUV";

    @Override
    protected String getPreviewOutputName () {
        return OUTPUT_UV;
    }

    @Override
    protected void inputStateChanged (boolean isInputDynamic) {
        showShaderBox();
    }

    @Override
    protected boolean isInputDynamic () {
        return true;
    }

    @Override
    public void prepareDeclarations (ShaderBuilder shaderBuilder) {

    }

    @Override
    public String writeOutputCode (String slotId) {
        return "v_texCoords";
    }
}
