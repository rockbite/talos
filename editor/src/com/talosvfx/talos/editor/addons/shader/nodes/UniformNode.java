package com.talosvfx.talos.editor.addons.shader.nodes;

import com.talosvfx.talos.editor.nodes.widgets.AbstractWidget;
import com.talosvfx.talos.editor.nodes.widgets.ValueWidget;
import com.talosvfx.talos.runtime.vfx.shaders.ShaderBuilder;

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

    public void setValue(float value) {
        AbstractWidget val = widgetMap.get("val");
        if(val instanceof ValueWidget) {
            ValueWidget valueWidget = (ValueWidget) val;
            valueWidget.setValue(value);
        }
    }

    @Override
    public String getValueDescriptor () {
        return null;
    }

    public void setUniformName(String name) {
        uniformName = name;
    }
}
