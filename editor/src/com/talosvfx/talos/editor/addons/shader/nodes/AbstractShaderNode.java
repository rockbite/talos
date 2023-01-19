package com.talosvfx.talos.editor.addons.shader.nodes;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.runtime.vfx.shaders.ShaderBuilder;
import com.talosvfx.talos.editor.addons.shader.widgets.ShaderBox;
import com.talosvfx.talos.editor.nodes.NodeBoard;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.nodes.widgets.AbstractWidget;
import com.talosvfx.talos.editor.nodes.widgets.ColorWidget;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.dynamicnodestage.NodeConnectionCreatedEvent;
import com.talosvfx.talos.editor.notifications.events.dynamicnodestage.NodeConnectionRemovedEvent;
import com.talosvfx.talos.editor.notifications.events.dynamicnodestage.NodeDataModifiedEvent;
import com.talosvfx.talos.editor.notifications.events.dynamicnodestage.NodeRemovedEvent;
import com.talosvfx.talos.editor.utils.HeightAction;

public abstract class AbstractShaderNode extends NodeWidget implements Observer {

    protected ShaderBuilder previewBuilder = new ShaderBuilder();

    protected ObjectMap<String, String> inputStrings = new ObjectMap<>();

    protected ShaderBox shaderBox;
    protected Cell shaderBoxCell;
    private ObjectIntMap<String> sizeMap = new ObjectIntMap<>();

    public abstract void prepareDeclarations(ShaderBuilder shaderBuilder);

    public abstract String writeOutputCode(String slotId);

    private boolean isInputDynamic = false;

    public static final int CAST_STRATEGY_ZERO = 0;
    public static final int CAST_STRATEGY_REPEAT = 1;

    private boolean isProcessed = false;

    private float previewUpdateCooldown = 0f;
    private boolean previewUpdateScheduled = false;

    public AbstractShaderNode() {
        sizeMap.put(ShaderBuilder.Type.VEC2.getTypeString(), 2);
        sizeMap.put(ShaderBuilder.Type.VEC3.getTypeString(), 3);
        sizeMap.put(ShaderBuilder.Type.VEC4.getTypeString(), 4);

        Notifications.registerObserver(this);
    }

    @EventHandler
    public void onNodeDataModifiedEvent(NodeDataModifiedEvent event) {
        updatePreviewIfNeeded();
    }

    @EventHandler
    public void onNodeRemovedEvent(NodeRemovedEvent event) {
        updatePreviewIfNeeded();
    }

    @EventHandler
    public void onNodeConnectionCreatedEvent(NodeConnectionCreatedEvent event) {
        updatePreviewIfNeeded();
    }

    @EventHandler
    public void onNodeConnectionRemovedEvent(NodeConnectionRemovedEvent event) {
        updatePreviewIfNeeded();
    }

    protected void updatePreviewIfNeeded() {

        if(previewUpdateCooldown > 0) {
            previewUpdateScheduled = true;
            return;
        }

        // TODO: only update nodes that are affected by this update (as in are parent of)
        // TODO: have a time delay to not do this very often
        if(shaderBox != null && isInputDynamic()) {
            updatePreview();
            previewUpdateCooldown = 0.1f;
        }
        previewUpdateScheduled = false;
    }

    @Override
    public void init (Skin skin, NodeBoard nodeBoard) {
        super.init(skin, nodeBoard);
    }

    @Override
    public void constructNode (XmlReader.Element module) {
        super.constructNode(module);

        inputStateChanged(isInputDynamic);

        if(isInputDynamic()) {
            updatePreviewIfNeeded();
        }
    }

    public void resetProcessingTree() {
        isProcessed = false;

        ObjectMap<String, Array<Connection>> inputs = getInputs();

        for(String id : inputs.keys()) {
            for(Connection connection: inputs.get(id)) {
                AbstractShaderNode node = (AbstractShaderNode) connection.targetNode;

                node.resetProcessingTree();
            }
        }
    }

    public void processTree(ShaderBuilder shaderBuilder) {
        if(isProcessed) {
            return;
        }

        inputStrings.clear();

        ObjectMap<String, Array<Connection>> inputs = getInputs();

        if(isInputDynamic != inputs.size > 0) {
            isInputDynamic = inputs.size > 0;
            inputStateChanged(isInputDynamic);
        }

        for(String id : inputs.keys()) {
            for(Connection connection: inputs.get(id)) {
                AbstractShaderNode node = (AbstractShaderNode) connection.targetNode;

                node.processTree(shaderBuilder);

                String returnStatement = node.writeOutputCode(connection.targetSlot);
                inputStrings.put(id, returnStatement);
            }
        }

        prepareDeclarations(shaderBuilder);

        isProcessed = true;
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

        if(!isInputDynamic() && isInputDynamic) {
            isInputDynamic = false;
            inputStateChanged(isInputDynamic);
        }

        previewUpdateCooldown-= delta;

        if(previewUpdateCooldown <= 0) {
            previewUpdateCooldown = 0;

            if(previewUpdateScheduled) {
                updatePreviewIfNeeded();
            }
        }
    }

    protected boolean isInputDynamic() {
        return getInputs().size > 0;
    }

    protected void updatePreview() {
        previewBuilder.reset();

        resetProcessingTree();
        processTree(previewBuilder);

        String val = writeOutputCode(getPreviewOutputName());
        previewBuilder.addLine(getPreviewLine(val));

        shaderBox.setShader(previewBuilder);
    }

    protected String getPreviewLine(String expression) {
        ShaderBuilder.Type outputType = getVarType(getPreviewOutputName());

        expression = castTypes(expression, outputType, ShaderBuilder.Type.VEC4, CAST_STRATEGY_REPEAT);

        return "vec4 outputVal = " + expression + "; outputVal.a = 1.0; return outputVal";
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

        if(varType == ShaderBuilder.Type.FLUID) {
            varType = targetType;
        }

        if(val == null) {
            ShaderBuilder.Type autoType = ShaderBuilder.Type.FLOAT;
            if(widgetMap.get(slot) instanceof ColorWidget) {
                autoType = ShaderBuilder.Type.VEC4;
            }

            targetType = autoType;
            varType = autoType;
        }

        if(targetType != varType) {
            val = castTypes(val, targetType, varType, CAST_STRATEGY_REPEAT);
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

    public ShaderBuilder.Type getMaxType(ShaderBuilder.Type a,  ShaderBuilder.Type b) {
        if(a == b) return a;

        if(a == null) return b;
        if(b == null) return a;

        int sizeA = sizeMap.get(a.getTypeString(), 0);
        int sizeB = sizeMap.get(b.getTypeString(), 0);
        if(a == ShaderBuilder.Type.FLOAT) sizeA = 1;
        if(b == ShaderBuilder.Type.FLOAT) sizeB = 1;

        if (sizeA > sizeB) {
            return a;
        } else {
            return b;
        }
    }

    public String castTypes(String expression, ShaderBuilder.Type fromType,  ShaderBuilder.Type toType) {
        return castTypes(expression, fromType, toType, CAST_STRATEGY_ZERO);
    }

    public String castTypes(String expression, ShaderBuilder.Type fromType,  ShaderBuilder.Type toType, int castStrategy) {

        if (fromType == toType) {
            return expression;
        }

        String fillVal = "0.0";

        if(castStrategy == CAST_STRATEGY_REPEAT) {
            fillVal = expression;
        }

        if(fromType == ShaderBuilder.Type.FLOAT) {
            if (toType == ShaderBuilder.Type.VEC4) {
                expression = "vec4(" + expression + ", " + fillVal + ", " + fillVal + ", 1.0)";
            } else {
                expression = toType.getTypeString() + "(" + expression + ")";
            }
        } else {
            if(toType == ShaderBuilder.Type.FLOAT) {
                expression = expression + ".x";
            } else {
                String[] fields = ShaderBuilder.fields;

                String newExpression = toType.getTypeString() + "(";

                for(int i = 0; i < sizeMap.get(toType.getTypeString(), 0); i++) {
                    int sizeToPut = sizeMap.get(fromType.getTypeString(), 0);
                    if(i < sizeToPut) {
                        String field = fields[i];
                        String add = "("+expression+")." + field;
                        newExpression += add + ",";
                    } else {
                        if(i == 3) {
                            newExpression += "1.0,";
                        } else {
                            newExpression += "0.0,";
                        }
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
        } else if(typeString.equals("vec3")) {
            return ShaderBuilder.Type.VEC3;
        } else if(typeString.equals("vec4")) {
             return ShaderBuilder.Type.VEC4;
        } else {
            return ShaderBuilder.Type.FLUID;
        }
    }

    protected ShaderBuilder.Type getTargetVarType(String name) {
        return getTargetVarType(name, null);
    }

    protected ShaderBuilder.Type getTargetVarType(String name, ShaderBuilder.Type defaultType) {

        if(inputs.get(name) == null || inputs.get(name).isEmpty()) return defaultType;

        Connection connection = inputs.get(name).first();

        if(inputs.get(name) != null && connection.targetNode != null) {
            String removeVarName = connection.targetSlot;
            return ((AbstractShaderNode)connection.targetNode).getVarType(removeVarName);
        } else {
            return defaultType;
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

    public ShaderBox getShaderBox() {
        return shaderBox;
    }
}
