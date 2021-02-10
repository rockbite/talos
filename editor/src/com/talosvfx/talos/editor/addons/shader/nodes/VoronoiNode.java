package com.talosvfx.talos.editor.addons.shader.nodes;

import com.talosvfx.talos.runtime.shaders.ShaderBuilder;

public class VoronoiNode extends AbstractShaderNode {

    public final String INPUT_UV = "uv";
    public final String INPUT_AO = "angleOffset";
    public final String INPUT_CD = "cellDensity";

    public final String OUTPUT = "outputValue";

    @Override
    public void prepareDeclarations (ShaderBuilder shaderBuilder) {
        ShaderBuilder.Argument[] args = new ShaderBuilder.Argument[3];
        args[0] = new ShaderBuilder.Argument(ShaderBuilder.Type.VEC2, "uv");
        args[1] = new ShaderBuilder.Argument(ShaderBuilder.Type.FLOAT, "angleOffset");
        args[2] = new ShaderBuilder.Argument(ShaderBuilder.Type.FLOAT, "cellDensity");

        ShaderBuilder.Method method = shaderBuilder.addMethod(ShaderBuilder.Type.FLOAT, "voronoiNoise", args);

        String body = "" +
                "float outputValue = 0.0;\n" +
                "float cells = 0.0;\n" +
                "\n" +
                "vec2 g = floor(uv * cellDensity);\n" +
                "vec2 f = fract(uv * cellDensity);\n" +
                "float t = 8.0;\n" +
                "vec3 res = vec3(8.0, 0.0, 0.0);\n" +
                "\n" +
                "for(int y=-1; y<=1; y++)\n" +
                "{\n" +
                "    for(int x=-1; x<=1; x++)\n" +
                "    {\n" +
                "        vec2 lattice = vec2(x,y);\n" +
                "        \n" +
                "        vec2 latg = lattice + g;\n" +
                "\t\tmat2 m = mat2(15.27, 47.63, 99.41, 89.98);\n" +
                "    \tlatg = fract(sin(latg * m) * 46839.32);\n" +
                "    \tvec2 offset = vec2(sin(latg.y*+angleOffset)*0.5+0.5, cos(latg.x*angleOffset)*0.5+0.5);\n" +
                "\n" +
                "        float d = distance(lattice + offset, f);\n" +
                "        if(d < res.x)\n" +
                "        {\n" +
                "            res = vec3(d, offset.x, offset.y);\n" +
                "            outputValue = res.x;\n" +
                "            cells = res.y;\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "return outputValue";

        method.addLine(body);

        String ao = getExpression(INPUT_AO, null);
        String cd = getExpression(INPUT_CD, null);

        String uv = getExpression(INPUT_UV, "v_texCoords");

        shaderBuilder.declareVariable(ShaderBuilder.Type.FLOAT, "voronoiVar" + getId(), "voronoiNoise(" + uv + ", " + ao + ", " + cd + ")");
    }

    @Override
    public String writeOutputCode (String slotId) {
        return "voronoiVar" + getId();
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
