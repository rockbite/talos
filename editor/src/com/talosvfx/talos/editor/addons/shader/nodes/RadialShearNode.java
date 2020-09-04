package com.talosvfx.talos.editor.addons.shader.nodes;

import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;

public class RadialShearNode extends AbstractShaderNode {

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

        ShaderBuilder.Method method = shaderBuilder.addMethod(ShaderBuilder.Type.VEC2, "radialShear", args);

        String body = "" +
                "vec2 delta = uv - center;\n" +
                "float delta2 = dot(delta.xy, delta.xy);\n" +
                "vec2 delta_offset = vec2(delta2 * strength);\n" +
                "return uv + vec2(delta.y, -delta.x) * delta_offset + offset";

        method.addLine(body);

        String center = getExpression(CENTER, "vec2(0.5)");
        String strength = getExpression(STRENGTH, null);
        String offset = getExpression(OFFSET, "vec2(0.0)");
        String uv = getExpression(INPUT_UV, "v_texCoords");

        shaderBuilder.declareVariable(ShaderBuilder.Type.VEC2, "radialShearVar" + getId(), "radialShear(" + uv + ", " + center + ", " + strength + ", " + offset + ")");
    }

    @Override
    public String writeOutputCode (String slotId) {
        return "radialShearVar" + getId();
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
