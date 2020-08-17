package com.talosvfx.talos.editor.addons.shader.nodes;

import com.badlogic.gdx.utils.Align;
import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;
import com.talosvfx.talos.editor.addons.shader.widgets.ShaderBox;
import com.talosvfx.talos.editor.nodes.NodeWidget;

public class ColorOutput extends AbstractShaderNode {

    public final int INPUT_RGBA = 0;

    /*
    @Override
    protected void configureConnections () {
        addConnection("color", INPUT_RGBA, Align.left);
    }

    @Override
    protected void buildContent () {
        shaderBox = new ShaderBox();
        dynamicContentTable.add(shaderBox).padTop(30).growX().expand().height(150).padLeft(-17).padRight(-16);
    }*/

    public void prepareDeclarations (ShaderBuilder shaderBuilder) {

    }/*

    @Override
    public String writeOutputCode (int slotId) {
        return null;
    }

    public void buildFragmentShader(ShaderBuilder shaderBuilder) {
        shaderBuilder.reset();
        processTree(shaderBuilder);

        String color = inputStrings.get(INPUT_RGBA);

        if(color == null) {
            color = "vec4(0.0, 0.0, 0.0, 1.0)";
        }

        shaderBuilder.addLine("gl_FragColor = " + color + "");
    }

    @Override
    public void graphUpdated () {
        buildFragmentShader(previewBuilder);
    }
    */

    @Override
    public String writeOutputCode(String slotId) {
        return null;
    }
}
