package com.talosvfx.talos.runtime.shaders;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.talosvfx.talos.runtime.utils.ShaderDescriptor;

import java.util.Arrays;

public class ShaderBuilder {

    String mainContent = "";
    ShaderProgram shaderProgramCache;

    private ObjectMap<String, UniformData> declaredUniforms = new ObjectMap<>();
    private OrderedMap<String, Method> methodMap = new OrderedMap<>();
    private ObjectMap<String, String> variableMap = new ObjectMap<>();

    public static final String[] fields = {"r", "g", "b", "a"};

    private ObjectMap<String, String> resourceToUniformMap = new ObjectMap<>();
    private ObjectMap<String, String> uniformToResourceMap = new ObjectMap<>();

    private int resourceCounter = 0;
    private String codeOverride = null;
    private String vertShaderOverride = null;

    public ShaderBuilder() {

    }

    public void reset() {
        mainContent = "";
        shaderProgramCache = null;
        variableMap.clear();
        declaredUniforms.clear();

        resourceToUniformMap.clear();
        uniformToResourceMap.clear();

        resourceCounter = 0;
    }

    public static String DEFAULT_TEMPLATE() {
        String template =
                "#ifdef GL_ES\n" +
                "#define LOWP lowp\n" +
                "   precision mediump float;\n" +
                "#else\n" +
                "   #define LOWP\n" +
                "#endif\n\n";

        template +=
                "uniform sampler2D u_texture;\n" + //this is baddy
                "varying LOWP vec4 v_color;\n" +
                "varying vec2 v_texCoords;\n\n";

        template += "\n\n{CUSTOM_UNIFORMS}\n";
        template += "\n{CUSTOM_METHODS}\n";
        template += "\n{SHADER_LOGIC}\n";

        template += "void main() {\n";

        template += "gl_FragColor = getFragColor();\n";

        template += "}";

        return template;
    }

    public String generateFragmentResolve() {
        String code = "vec4 getFragColor() {\n" +
                mainContent + "\n" +
                "}\n";

        return code;
    }

    public String generateMethods() {
        String finalString = "";
        if (methodMap.size > 0) {

            for(String methodName: methodMap.keys()) {
                Method method = methodMap.get(methodName);

                finalString += method.getSource() + "\n";
            }

            finalString += "\n";
        }

        return finalString;
    }

    public String generateCustomUniforms() {
        String uniformString = "";
        for(UniformData uniformData: declaredUniforms.values()) {
            String line = "uniform " + uniformData.type.getTypeString() + " " + uniformData.variableName + ";";
            uniformString += line + "\n";
        }

        return uniformString;
    }

    public String getFragmentString () {
        return getFragmentString(DEFAULT_TEMPLATE());
    }

    public String getFragmentString (String template) {

        if(codeOverride != null) {
            return codeOverride;
        }

        template = template.replace("{CUSTOM_UNIFORMS}", generateCustomUniforms());
        template = template.replace("{CUSTOM_METHODS}", generateMethods());
        template = template.replace("{SHADER_LOGIC}", generateFragmentResolve());

        String finalString = template;

        return finalString;
    }

    public static String getStaticVertexString() {
        String result =
                "attribute vec3 a_position;\n" +
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
                        "    gl_Position =  u_projTrans * vec4(a_position, 1.0);\n" +
                        "}\n";


        return result;
    }

    public String getVertexString() {

        if(vertShaderOverride != null) {
            return vertShaderOverride;
        } else {
            return ShaderBuilder.getStaticVertexString();
        }
    }

    public ShaderProgram getShaderProgram() {
        if(shaderProgramCache != null) {
            return  shaderProgramCache;
        }

        String vert = getVertexString();
        String frag = getFragmentString(DEFAULT_TEMPLATE());

        ShaderProgram shaderProgram = new ShaderProgram(vert, frag);
        shaderProgram.pedantic = false;
        shaderProgramCache = shaderProgram;

        return shaderProgram;
    }

    public void addLine (String line) {
        mainContent += "    " + line + ";\n";
    }

    public Method addMethod(Method method) {
        methodMap.put(method.name, method);

        return method;
    }

    public Method addMethod(Type returnType, String name, Argument[] args) {
        Method method = new Method();

        method.name = name;
        method.returnType = returnType;

        method.args = Arrays.copyOf(args, args.length);

        methodMap.put(name, method);

        return method;
    }

    public void addLineToMethod (String methodName, String line) {
        Method method = methodMap.get(methodName);

        if(method != null) {
            method.body += line + ";\n";
        }
    }

    public void declareVariable (Type type, String name, String value) {
        if(!variableMap.containsKey(name)) {
            addLine(type.getTypeString() + " " + name + " = " + value);
            variableMap.put(name, value);
        }
    }

    public void setShader (String code) {
        codeOverride = code;
    }

    public void setVertOverride(String code) {
        vertShaderOverride = code;
    }

    public static class Method {
        public Type returnType;
        public String name;
        public Argument[] args;

        public String body = "";

        // override for simple cases
        public String declaration;

        public void addLine(String line) {
            body += "    " + line + ";\n";
        }

        public String getSource () {
            String finalString = "";

            if(declaration != null) {
                finalString += declaration + " {\n" + body + "\n}";
            } else {

                finalString += returnType.typeString + " " + name + "(";
                for (int i = 0; i < args.length; i++) {
                    finalString += args[i].type.typeString + " " + args[i].name;
                    if (i < args.length - 1) {
                        finalString += ", ";
                    }
                }
                finalString += ") {\n" + body + "\n}";
            }

            return finalString;
        }

        public void setBody (String body) {
            this.body = body;
        }
    }

    public static class Argument {
        public Type type;
        public String name;

        public Argument(Type type, String name) {
            this.type = type;
            this.name = name;
        }
    }

    public class UniformData {
        public Type type;
        public String variableName;
        public IValueProvider payload;
    }

    public interface IValueProvider<T> {
        T getValue();
        String getValueDescriptor ();
    }

    public enum Type {
        FLOAT("float"),
        INT("int"),
        VEC2("vec2"),
        VEC3("vec3"),
        VEC4("vec4"),
        FLUID("fluid"),
        TEXTURE("sampler2D");

        private String typeString;

        Type(String type) {
            this.typeString = type;
        }

        public String getTypeString() {
            return typeString;
        }
    }

    public void declareUniform(String name, Type type, IValueProvider value) {
        if(declaredUniforms.containsKey(name)) {
            return;
        }
        UniformData uniformData = new UniformData();
        uniformData.variableName = name;
        uniformData.type = type;
        uniformData.payload = value;

        declaredUniforms.put(name, uniformData);
    }

    public ObjectMap<String, UniformData> getDeclaredUniforms() {
        return declaredUniforms;
    }

    public String registerResource(String name) {

        if(resourceToUniformMap.containsKey(name)) {
            return resourceToUniformMap.get(name);
        }

        String uniformName = "u_texture" + (resourceCounter++);

        resourceToUniformMap.put(name, uniformName);
        uniformToResourceMap.put(uniformName, name);

        return uniformName;
    }

    public String getMainContent() {
        return mainContent;
    }

    public static String compileShaderString(ShaderDescriptor shaderDescriptor, String template)  {
        String logic = shaderDescriptor.getShaderLogic();
        String methods = shaderDescriptor.getCustomMethods();
        String uniforms = shaderDescriptor.getCustomUniforms();

        logic = "vec4 getFragColor() {\n" +
                logic + "\n" +
                "}\n";

        template = template.replace("{CUSTOM_UNIFORMS}", uniforms);
        template = template.replace("{CUSTOM_METHODS}", methods);
        template = template.replace("{SHADER_LOGIC}", logic);

        return template;
    }


    public static ShaderProgram compileShader(ShaderDescriptor shaderDescriptor, String template) {
        String code = compileShaderString(shaderDescriptor, template);

        ShaderProgram shaderProgram = new ShaderProgram(ShaderBuilder.getStaticVertexString(), code);

        return shaderProgram;
    }
}
