package com.talosvfx.talos.editor.addons.shader.nodes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;
import com.talosvfx.talos.editor.addons.shader.widgets.ShaderBox;
import com.talosvfx.talos.editor.nodes.NodeBoard;
import com.talosvfx.talos.editor.nodes.NodeWidget;

public abstract class AbstractShaderNode extends NodeWidget {

    protected ShaderBuilder previewBuilder = new ShaderBuilder();

    protected ShaderBox shaderBox;

    protected ObjectMap<String, String> inputStrings = new ObjectMap<>();

    public abstract void prepareDeclarations(ShaderBuilder shaderBuilder);

    public abstract String writeOutputCode(String slotId);

    @Override
    public void init (Skin skin, NodeBoard nodeBoard) {
        super.init(skin, nodeBoard);
    }

    public void processTree(ShaderBuilder shaderBuilder) {
        inputStrings.clear();
        ObjectMap<String, NodeWidget.Connection> inputs = getInputs();
        for(String id : inputs.keys()) {
            NodeWidget.Connection connection = inputs.get(id);
            AbstractShaderNode node = (AbstractShaderNode)connection.targetNode;

            node.processTree(shaderBuilder);

            String returnStatement = node.writeOutputCode(connection.targetSlot);
            inputStrings.put(id, returnStatement);
        }

        prepareDeclarations(shaderBuilder);
    }

    protected void previewOutput(String slotId) {
        previewBuilder.reset();
        processTree(previewBuilder);
        String val = writeOutputCode(slotId);
        previewBuilder.addLine("gl_FragColor = " + val);
    }

    @Override
    public void graphUpdated () {
        super.graphUpdated();
        //previewOutput(0);
    }

    @Override
    public void act (float delta) {
        super.act(delta);

        if(shaderBox != null) {
            shaderBox.setShader(previewBuilder);
        }
    }
}
