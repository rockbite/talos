package com.talosvfx.talos.editor.addons.shader.nodes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.addons.shader.widgets.ShaderBox;
import com.talosvfx.talos.editor.addons.shader.widgets.ShaderBox3D;
import com.talosvfx.talos.runtime.shaders.ShaderBuilder;

public class MaterialEndpoint extends AbstractShaderNode {

    public final String INPUT_SHADER = "inputShader";

    public final String PREVIEW_TYPE = "preview";
    private ShaderBox box2d;
    private ShaderBox3D box3d;

    @Override
    public void constructNode (XmlReader.Element module) {
        super.constructNode(module);

        widgetMap.get(PREVIEW_TYPE).addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent changeEvent, Actor actor) {
                String value = (String) widgetMap.get(PREVIEW_TYPE).getValue();

                if (value.equals("2D")) {
                    box2d.setVisible(true);
                    box3d.setVisible(false);

                    shaderBox = box2d;
                } else if (value.equals("3D")) {
                    box2d.setVisible(false);
                    box3d.setVisible(true);

                    shaderBox = box3d;
                }
            }
        });

        setClip(false);

        box2d.setVisible(false);
        box3d.setVisible(true);
        shaderBox = box3d;
    }

    public void prepareDeclarations (ShaderBuilder shaderBuilder) {

    }

    public void buildFragmentShader(ShaderBuilder shaderBuilder) {
        shaderBuilder.reset();

        resetProcessingTree();
        processTree(shaderBuilder);

        String code = getExpression(INPUT_SHADER);

        shaderBuilder.setShader(code);
        shaderBuilder.setVertOverride(Gdx.files.internal("addons/shader/shaders/default.vert.glsl").readString());
    }

    @Override
    public void graphUpdated () {
        buildFragmentShader(previewBuilder);
        super.graphUpdated();
    }


    @Override
    protected void updatePreview () {
        box2d.setShader(previewBuilder);
        box3d.setShader(previewBuilder);
    }

    @Override
    protected String getPreviewOutputName () {
        return null;
    }

    @Override
    public String writeOutputCode(String slotId) {
        return null;
    }

    @Override
    protected void addAdditionalContent (Table contentTable) {
        box2d = new ShaderBox();
        box3d = new ShaderBox3D();

        Stack stack = new Stack();

        stack.add(box2d);
        stack.add(box3d);

        box3d.setVisible(false);

        shaderBox = box2d;

        shaderBoxCell = contentTable.add(stack);
        shaderBoxCell.height(0).width(240).padTop(10).row();
    }
}
