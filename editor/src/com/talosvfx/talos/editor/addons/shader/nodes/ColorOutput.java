package com.talosvfx.talos.editor.addons.shader.nodes;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;
import com.talosvfx.talos.editor.addons.shader.widgets.ShaderBox;
import com.talosvfx.talos.editor.nodes.NodeWidget;

public class ColorOutput extends AbstractShaderNode {

    public final String INPUT_RGBA = "inputColor";

    public final String BLENDING = "blending";

    private ShaderBox.Blending blending = ShaderBox.Blending.NORMAL;

    public ColorOutput (Skin skin) {
        super(skin);
    }

    @Override
    public void constructNode (XmlReader.Element module) {
        super.constructNode(module);

        widgetMap.get(BLENDING).addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent changeEvent, Actor actor) {
                String blendingValue = (String) widgetMap.get(BLENDING).getValue();

                if (blendingValue.equals("NORMAL")) {
                    blending = ShaderBox.Blending.NORMAL;
                } else if (blendingValue.equals("ADDITIVE")) {
                    blending = ShaderBox.Blending.ADDITIVE;
                } else if (blendingValue.equals("BLEND-ADD")) {
                    blending = ShaderBox.Blending.BLENDADD;
                }

                shaderBox.setBlending(blending);
            }
        });
    }

    public void prepareDeclarations (ShaderBuilder shaderBuilder) {

    }

    public void buildFragmentShader(ShaderBuilder shaderBuilder) {
        shaderBuilder.reset();

        resetProcessingTree();
        processTree(shaderBuilder);

        String color = getExpression(INPUT_RGBA);

        if(color == null) {
            color = "vec4(0.0, 0.0, 0.0, 1.0)";
        }

        shaderBuilder.addLine("gl_FragColor = " + color + "");
    }

    @Override
    public void graphUpdated () {
        buildFragmentShader(previewBuilder);
        super.graphUpdated();
    }


    @Override
    protected void updatePreview () {
        shaderBox.setShader(previewBuilder);
    }

    @Override
    protected String getPreviewOutputName () {
        return null;
    }

    @Override
    public String writeOutputCode(String slotId) {
        return null;
    }
}
