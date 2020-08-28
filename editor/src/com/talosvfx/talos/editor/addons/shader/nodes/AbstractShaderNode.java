package com.talosvfx.talos.editor.addons.shader.nodes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.actions.MoveByAction;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;
import com.talosvfx.talos.editor.addons.shader.widgets.ShaderBox;
import com.talosvfx.talos.editor.nodes.NodeBoard;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.utils.HeightAction;

public abstract class AbstractShaderNode extends NodeWidget {

    protected ShaderBuilder previewBuilder = new ShaderBuilder();

    protected ObjectMap<String, String> inputStrings = new ObjectMap<>();

    protected ShaderBox shaderBox;
    protected Cell<ShaderBox> shaderBoxCell;

    public abstract void prepareDeclarations(ShaderBuilder shaderBuilder);

    public abstract String writeOutputCode(String slotId);

    private boolean isInputDynamic = false;

    @Override
    public void init (Skin skin, NodeBoard nodeBoard) {
        super.init(skin, nodeBoard);
    }

    @Override
    public void constructNode (XmlReader.Element module) {
        super.constructNode(module);

        inputStateChanged(isInputDynamic);
    }

    public void processTree(ShaderBuilder shaderBuilder) {
        inputStrings.clear();

        ObjectMap<String, NodeWidget.Connection> inputs = getInputs();

        if(isInputDynamic != inputs.size > 0) {
            isInputDynamic = inputs.size > 0;
            inputStateChanged(isInputDynamic);
        }

        for(String id : inputs.keys()) {
            NodeWidget.Connection connection = inputs.get(id);
            AbstractShaderNode node = (AbstractShaderNode)connection.targetNode;

            node.processTree(shaderBuilder);

            String returnStatement = node.writeOutputCode(connection.targetSlot);
            inputStrings.put(id, returnStatement);
        }

        prepareDeclarations(shaderBuilder);
    }

    protected void inputStateChanged (boolean isInputDynamic) {
        // need to hid shaderbox if no input
        if(isInputDynamic) {
            showShaderBox();
        } else {
            hideShaderBox();
        }
    }

    protected void showShaderBox () {
        HeightAction<Cell<ShaderBox>> cellHeightAction = new HeightAction<>();

        cellHeightAction.setDuration(0.1f);
        cellHeightAction.setTarget(240);
        cellHeightAction.setTarget(shaderBoxCell);
        this.addAction(cellHeightAction);
    }

    protected void hideShaderBox () {
        HeightAction<Cell<ShaderBox>> cellHeightAction = new HeightAction<>();

        cellHeightAction.setDuration(0.1f);
        cellHeightAction.setTarget(0);
        cellHeightAction.setTarget(shaderBoxCell);
        this.addAction(cellHeightAction);
    }

    @Override
    protected void addAdditionalContent(Table contentTable) {
        shaderBox = new ShaderBox();
        shaderBoxCell = contentTable.add(shaderBox);

        shaderBoxCell.height(0).width(240).padTop(10).row();
    }

    @Override
    public void graphUpdated () {
        super.graphUpdated();
    }

    @Override
    public void act (float delta) {
        super.act(delta);

        if(shaderBox != null && shaderBox.isVisible()) {
            updatePreview();
        }
    }

    protected void updatePreview() {
        previewBuilder.reset();
        processTree(previewBuilder);
        String val = writeOutputCode(getPreviewOutputName());
        previewBuilder.addLine("gl_FragColor = " + val);

        shaderBox.setShader(previewBuilder);
    }

    protected String getPreviewOutputName () {
        return "outputValue";
    }

    public String getExpression(String slot) {
        return getExpression(slot, "0.0");
    }

    public String getExpression(String slot, String def) {
        String val = inputStrings.get(slot);
        if(val == null) {
            if(def == null) {
                val = widgetMap.get(slot).getValue() + "";
            } else {
                val = def;
            }
        }

        return val;
    }

}
