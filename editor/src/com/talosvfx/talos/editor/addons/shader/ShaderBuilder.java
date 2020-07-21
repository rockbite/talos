package com.talosvfx.talos.editor.addons.shader;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.ObjectMap;

public class ShaderBuilder {

    String mainContent;
    ShaderProgram shaderProgramCache;

    private ObjectMap<String, UniformData> declaredUniforms = new ObjectMap<>();

    public ShaderBuilder() {

    }

    public void reset() {
        mainContent = "";
        shaderProgramCache = null;
    }

    public String getFragmentString () {

        String finalString = "";
        finalString +=
                "#ifdef GL_ES\n" +
                "#define LOWP lowp\n" +
                "   precision mediump float;\n" +
                "#else\n" +
                "   #define LOWP\n" +
                "#endif\n\n";

        // add declarations here
        finalString +=
                "uniform sampler2D u_texture;\n" + //this needs removing
                "varying LOWP vec4 v_color;\n" +
                "varying vec2 v_texCoords;\n\n";

        for(UniformData uniformData: declaredUniforms.values()) {
            String line = "uniform " + uniformData.type.getTypeString() + " " + uniformData.variableName + ";";
            finalString += line + "\n";
        }

        finalString += "\n";

        finalString += "void main() {\n";

        finalString += mainContent;

        finalString += "}";

        return finalString;
    }

    public String getVertexString() {
        String result =
                "attribute vec4 a_position;\n" +
                "attribute vec4 a_color;\n" +
                "attribute vec2 a_texCoord0;\n" +
                "\n" +
                "uniform mat4 u_projTrans;\n" +
                "\n" +
                "varying vec4 v_color;\n" +
                "varying vec2 v_texCoords;\n" +
                "\n" +
                "void main()\n" +
                "{\n" +
                "    v_color = a_color;\n" +
                "    v_color.a = v_color.a * (256.0/255.0);\n" +
                "    v_texCoords = a_texCoord0;\n" +
                "    gl_Position =  u_projTrans * a_position;\n" +
                "}\n";


        return result;
    }

    public ShaderProgram getShaderProgram() {
        if(shaderProgramCache != null) {
            return  shaderProgramCache;
        }

        String vert = getVertexString();
        String frag = getFragmentString();

        ShaderProgram shaderProgram = new ShaderProgram(vert, frag);
        shaderProgram.pedantic = false;
        shaderProgramCache = shaderProgram;

        return shaderProgram;
    }

    public void addLine (String line) {
        mainContent += line + ";\n";
    }

    public class UniformData {
        public Type type;
        public String variableName;
        public Object payload;
    }

    public enum Type {
        FLOAT("float"),
        INT("int"),
        VEC2("vec2"),
        VEC3("vec3"),
        VEC4("vec4"),
        TEXTURE("sampler2D");

        private String typeString;

        Type(String type) {
            this.typeString = type;
        }

        public String getTypeString() {
            return typeString;
        }
    }

    public void declareUniform(String name, Type type, Object value) {
        UniformData uniformData = new UniformData();
        uniformData.variableName = name;
        uniformData.type = type;
        uniformData.payload = value;

        declaredUniforms.put(name, uniformData);
    }

    public ObjectMap<String, UniformData> getDeclaredUniforms() {
        return declaredUniforms;
    }
}
