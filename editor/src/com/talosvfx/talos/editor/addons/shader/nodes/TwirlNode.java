package com.talosvfx.talos.editor.addons.shader.nodes;

import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;

public class TwirlNode extends AbstractShaderNode {

    public final String INPUT_UV = "uv";
    public final String STRENGTH = "strength";
    public final String CENTER = "center";
    public final String OFFSET = "offset";

    public final String OUTPUT = "outputValue";

    @Override
    public void prepareDeclarations (ShaderBuilder shaderBuilder) {
        ShaderBuilder.Argument[] args = new ShaderBuilder.Argument[4];
        args[0] = new ShaderBuilder.Argument(ShaderBuilder.Type.VEC2, INPUT_UV);
        args[1] = new ShaderBuilder.Argument(ShaderBuilder.Type.VEC2, CENTER);
        args[2] = new ShaderBuilder.Argument(ShaderBuilder.Type.FLOAT, STRENGTH);
        args[3] = new ShaderBuilder.Argument(ShaderBuilder.Type.VEC2, OFFSET);

        ShaderBuilder.Method method = shaderBuilder.addMethod(ShaderBuilder.Type.VEC2, "twirl", args);

        String body = "vec2 delta = uv - center;\n" +
                "float angle = strength * length(delta);\n" +
                "float x = cos(angle) * delta.x - sin(angle) * delta.y;\n" +
                "float y = sin(angle) * delta.x + cos(angle) * delta.y;\n" +
                "return vec2(x + center.x + offset.x, y + center.y + offset.y)";

        method.addLine(body);

        String center = getExpression(CENTER, "vec2(0.5)");
        String strength = getExpression(STRENGTH, null);
        String offset = getExpression(OFFSET, "vec2(0.0)");
        String uv = getExpression(INPUT_UV, "v_texCoords");

        shaderBuilder.declareVariable(ShaderBuilder.Type.VEC2, "twirlVar" + getId(), "twirl(" + uv + ", " + center + ", " + strength + ", " + offset + ")");
    }

    @Override
    public String writeOutputCode (String slotId) {
        return "twirlVar" + getId();
    }

    @Override
    protected void inputStateChanged (boolean isInputDynamic) {
        showShaderBox();
    }

    @Override
    protected boolean isInputDynamic () {
        return true;
    }
}

