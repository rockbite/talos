package com.talosvfx.talos.editor.addons.shader.nodes;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;

public class TimeNode extends AbstractShaderNode {

    private ShaderBuilder.IValueProvider timeProvider;

    private float time = 0;

    @Override
    public void act (float delta) {
        super.act(delta);

        time += delta;

        //time = (float) (time - Math.floor(time));
    }

    @Override
    public void constructNode (XmlReader.Element module) {
        super.constructNode(module);

        timeProvider = new ShaderBuilder.IValueProvider() {
            @Override
            public float getValue () {
                return time;
            }
        };
    }

    @Override
    public void prepareDeclarations (ShaderBuilder shaderBuilder) {
        shaderBuilder.declareUniform("u_time" + getId(), ShaderBuilder.Type.FLOAT, timeProvider);
    }

    @Override
    public String writeOutputCode (String slotId) {
        return "u_time" + getId();
    }
}
