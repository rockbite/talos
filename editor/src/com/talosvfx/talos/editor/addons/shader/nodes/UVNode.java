package com.talosvfx.talos.editor.addons.shader.nodes;

import com.talosvfx.talos.runtime.shaders.ShaderBuilder;

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

    protected String getPreviewLine(String expression) {
        return "gl_FragColor = vec4(v_texCoords.x, v_texCoords.y, 0.0, 1.0)";
    }

    @Override
    public String writeOutputCode (String slotId) {
        return "v_texCoords";
    }
}
