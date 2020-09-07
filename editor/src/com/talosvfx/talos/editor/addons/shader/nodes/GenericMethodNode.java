package com.talosvfx.talos.editor.addons.shader.nodes;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;

public class GenericMethodNode extends AbstractShaderNode {

    protected String methodName = "";

    protected String shaderBodyString;
    protected String varName;

    protected Array<ShaderBuilder.Method> methods = new Array<>();

    private boolean forcePreview = true;
    private boolean previewTransparency = false;

    @Override
    public void constructNode (XmlReader.Element module) {
        XmlReader.Element shaderBody = module.getChildByName("shader-body");
        shaderBodyString = shaderBody.getText();

        methods.clear();
        if(module.getChildrenByName("method").size > 0) {
            Array<XmlReader.Element> methodsData = module.getChildrenByName("method");
            for(XmlReader.Element data: methodsData) {
                ShaderBuilder.Method method = new ShaderBuilder.Method();
                method.name = data.getAttribute("name");
                method.declaration = data.getAttribute("declaration");
                method.setBody(data.getText());
                methods.add(method);
            }
        }

        String name = module.getAttribute("name");

        methodName = name.substring(0, 1).toLowerCase() + name.substring(1);

        forcePreview = module.getBooleanAttribute("forcePreview", true);
        previewTransparency = module.getBooleanAttribute("previewTransparency", false);

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

        for(ShaderBuilder.Method inline: methods) {
            shaderBuilder.addMethod(inline);
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

    protected String getPreviewLine(String expression) {
        ShaderBuilder.Type outputType = getVarType(getPreviewOutputName());

        expression = castTypes(expression, outputType, ShaderBuilder.Type.VEC4, CAST_STRATEGY_REPEAT);

        String result =  "gl_FragColor = " + expression + ";";

        if(!previewTransparency) {
            result += " gl_FragColor.a = 1.0;";
        }

        return result;
    }
}
