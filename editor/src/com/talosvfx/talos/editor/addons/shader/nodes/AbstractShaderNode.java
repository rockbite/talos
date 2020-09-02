package com.talosvfx.talos.editor.addons.shader.nodes;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;
import com.talosvfx.talos.editor.addons.shader.widgets.ShaderBox;
import com.talosvfx.talos.editor.nodes.NodeBoard;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.nodes.widgets.AbstractWidget;
import com.talosvfx.talos.editor.nodes.widgets.ColorWidget;
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

        if(shaderBox != null && isInputDynamic()) {
            updatePreview();
        }

        if(!isInputDynamic() && isInputDynamic) {
            isInputDynamic = false;
            inputStateChanged(isInputDynamic);
        }
    }

    protected boolean isInputDynamic() {
        return getInputs().size > 0;
    }

    protected void updatePreview() {
        previewBuilder.reset();
        processTree(previewBuilder);
        String val = writeOutputCode(getPreviewOutputName());
        previewBuilder.addLine(getPreviewLine(val));

        shaderBox.setShader(previewBuilder);
    }

    protected String getPreviewLine(String expression) {
        return "gl_FragColor = vec4(" + expression + ")";
    }

    protected String getPreviewOutputName () {
        return "outputValue";
    }

    public String getExpression(String slot) {
        return getExpression(slot, "0.0");
    }

    public String getExpression(String slot, String def) {
        String val = inputStrings.get(slot);

        //  check type compatibility
        ShaderBuilder.Type targetType = getTargetVarType(slot);
        ShaderBuilder.Type varType = getVarType(slot);

        if(val == null) {
            ShaderBuilder.Type autoType = ShaderBuilder.Type.FLOAT;
            if(widgetMap.get(slot) instanceof ColorWidget) {
                autoType = ShaderBuilder.Type.VEC4;
            }

            targetType = autoType;
            varType = autoType;
        }

        if(targetType != varType) {
            val = castTypes(val, targetType, varType);
        }

        if(val == null) {
            if(def == null) {
                if(widgetMap.get(slot) instanceof ColorWidget) {
                    Color clr = ((ColorWidget) widgetMap.get(slot)).getValue();
                    val = "(vec4(" + clr.r + ", " + clr.g + ", " + clr.b + ", " + clr.a + "))";
                } else {
                    val = widgetMap.get(slot).getValue() + "";
                }
            } else {
                val = def;
            }
        }

        return val;
    }

    public String castTypes(String expression, ShaderBuilder.Type fromType,  ShaderBuilder.Type toType) {

        if(fromType == ShaderBuilder.Type.FLOAT) {
            expression = toType.getTypeString() + "(" + expression + ")";
        } else {
            if(toType == ShaderBuilder.Type.FLOAT) {
                expression = expression + ".x";
            } else {
                String[] fields = {"r", "g", "b", "a"};
                ObjectIntMap<String> sizeMap = new ObjectIntMap<>();
                sizeMap.put(ShaderBuilder.Type.VEC2.getTypeString(), 2);
                sizeMap.put(ShaderBuilder.Type.VEC3.getTypeString(), 4);
                sizeMap.put(ShaderBuilder.Type.VEC4.getTypeString(), 4);

                String newExpression = toType.getTypeString() + "(";

                for(int i = 0; i < sizeMap.get(toType.getTypeString(), 0); i++) {
                    int sizeToPut = sizeMap.get(fromType.getTypeString(), 0);
                    if(i < sizeToPut) {
                        String field = fields[i];
                        newExpression += "("+expression+")." + field + ",";
                    } else {
                        newExpression += "0.0,";
                    }
                }
                newExpression = newExpression.substring(0, newExpression.length() - 1) + ")";
                expression = newExpression;
            }
        }

        return expression;
    }

    public ShaderBuilder.Type getVarType(String name) {
        String typeString = typeMap.get(name);
        if(typeString.equals("float")) {
            return ShaderBuilder.Type.FLOAT;
        } else if(typeString.equals("vec2")) {
            return ShaderBuilder.Type.VEC2;
        } if(typeString.equals("vec3")) {
            return ShaderBuilder.Type.VEC3;
        }if(typeString.equals("vec4")) {
            return ShaderBuilder.Type.VEC4;
        } else {
            return ShaderBuilder.Type.FLOAT;
        }
    }

    protected ShaderBuilder.Type getTargetVarType(String name) {
        if(inputs.get(name) != null && inputs.get(name).targetNode != null) {
            String removeVarName = inputs.get(name).targetSlot;
            return ((AbstractShaderNode)inputs.get(name).targetNode).getVarType(removeVarName);
        } else {
            return null;
        }
    }

    @Override
    public void read (Json json, JsonValue jsonValue) {
        super.read(json, jsonValue);

        JsonValue properties = jsonValue.get("properties");

        for(String name: widgetMap.keys()) {
            JsonValue value = properties.get(name);

            if (value != null) {
                widgetMap.get(name).read(json, value);
            }
        }

        readProperties(properties);
    }

    @Override
    public void write (Json json) {
        super.write(json);

        json.writeObjectStart("properties");

        for(String name: widgetMap.keys()) {
            AbstractWidget widget = widgetMap.get(name);
            widget.write(json, name);
        }

        writeProperties(json);

        json.writeObjectEnd();
    }

    protected void readProperties(JsonValue properties) {

    }
    protected void writeProperties(Json json) {

    }

}
