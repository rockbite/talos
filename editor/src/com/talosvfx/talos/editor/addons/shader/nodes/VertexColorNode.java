package com.talosvfx.talos.editor.addons.shader.nodes;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;

public class VertexColorNode extends AbstractShaderNode {

    public final String OUTPUT = "outputValue";

    public VertexColorNode (Skin skin) {
        super(skin);
    }

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
