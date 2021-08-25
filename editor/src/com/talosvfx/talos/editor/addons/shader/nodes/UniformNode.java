package com.talosvfx.talos.editor.addons.shader.nodes;

import com.talosvfx.talos.runtime.shaders.ShaderBuilder;

public class UniformNode extends AbstractShaderNode implements ShaderBuilder.IValueProvider<Object> {

    private String uniformName;

    @Override
    public void prepareDeclarations (ShaderBuilder shaderBuilder) {
        shaderBuilder.declareUniform(uniformName, ShaderBuilder.Type.FLOAT, this);
    }

    @Override
    public String writeOutputCode (String slotId) {
        return null;
    }


    @Override
    public Object getValue () {
        return widgetMap.get("val").getValue();
    }

    @Override
    public String getValueDescriptor () {
        return null;
    }

    public void setUniformName(String name) {
        uniformName = name;
    }
}
