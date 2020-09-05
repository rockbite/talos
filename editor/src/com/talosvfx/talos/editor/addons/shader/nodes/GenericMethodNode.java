package com.talosvfx.talos.editor.addons.shader.nodes;

import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;

public class GenericMethodNode extends AbstractShaderNode {

    protected String methodName = "";

    protected String shaderBodyString;
    protected String varName;

    private boolean forcePreview = true;

    @Override
    public void constructNode (XmlReader.Element module) {
        XmlReader.Element shaderBody = module.getChildByName("shader-body");
        shaderBodyString = shaderBody.getText();

        String name = module.getAttribute("name");

        methodName = name.substring(0, 1).toLowerCase() + name.substring(1);

        forcePreview = module.getBooleanAttribute("forcePreview", true);

        super.constructNode(module);
    }

    @Override
    public void prepareDeclarations (ShaderBuilder shaderBuilder) {
        ShaderBuilder.Argument[] args = new ShaderBuilder.Argument[typeMap.size];

        int iterator = 0;
        String methodArgs = "";
        for(String inputName: typeMap.keys()) {
            ShaderBuilder.Type varType = getVarType(inputName);
            args[iterator++] = new ShaderBuilder.Argument(varType, inputName);

            String defaultVal = null;

            if(inputName.equals("uv")) {
                defaultVal = "v_texCoords";
            }

            String expr = getExpression(inputName, defaultVal);

            if(expr.equals("null")) {
                ShaderBuilder.Type type = getVarType(inputName);
                expr = type.getTypeString() + "(" + defaultsMap.get(inputName) + ")";
            }

            methodArgs +=  expr;

            if(iterator < typeMap.size) {
                methodArgs += ", ";
            }
        }

        varName = methodName + "Var";

        ShaderBuilder.Method method = shaderBuilder.addMethod(getVarType("outputValue"), methodName, args);

        method.setBody(shaderBodyString);

        String expression = methodName + "(" + methodArgs + ")";

        shaderBuilder.declareVariable(getVarType("outputValue"), varName + getId(), expression);

    }

    @Override
    public String writeOutputCode (String slotId) {
        return varName + getId();
    }

    @Override
    protected void inputStateChanged (boolean isInputDynamic) {
        if (forcePreview) {
            showShaderBox();
        } else {
            super.inputStateChanged(isInputDynamic);
        }
    }

    @Override
    protected boolean isInputDynamic () {
        if (forcePreview) {
            return true;
        } else {
            return super.isInputDynamic();
        }
    }
}
