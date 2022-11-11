package com.talosvfx.talos.runtime.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.runtime.shaders.ShaderBuilder;

public class ShaderDescriptor {

    public static class UniformData {
        public String name;
        public Type type;
        public String payload;
    }

    public enum Type {
        FLOAT("float"),
        VEC2("vec2"), VEC3("vec3"), VEC4("vec4"),
        TEXTURE("sampler2D");

        private String typeString;

        Type(String typeString) {
            this.typeString = typeString;
        }

        public static Type getFor(String type) {
            if(type.equals("float")) return FLOAT;
            if(type.equals("vec2")) return VEC2;
            if(type.equals("vec3")) return VEC3;
            if(type.equals("vec4")) return VEC4;
            if(type.equals("sampler2D")) return TEXTURE;

            return FLOAT;
        }

        public String getTypeString() {
            return typeString;
        }

    }

    private ObjectMap<String, UniformData> uniformMap = new ObjectMap<>();

    private String fragResolve;
    private String customMethods;

    public ShaderDescriptor() {

    }

    public ShaderDescriptor(FileHandle fileHandle) {
        setData(fileHandle.readString());
    }

    public void setData(String xmlString) {
        uniformMap.clear();

        XmlReader xmlReader = new XmlReader();
        XmlReader.Element shader = xmlReader.parse(xmlString);

        XmlReader.Element uniforms = shader.getChildByName("uniforms");
        XmlReader.Element main = shader.getChildByName("main");
        XmlReader.Element methods = shader.getChildByName("methods");

        fragResolve = main.getText();
        customMethods = methods.getText();

        for (XmlReader.Element uniformElement: uniforms.getChildrenByName("uniform")) {
            String name = uniformElement.getAttribute("name");
            String type = uniformElement.getAttribute("type");

            UniformData uniformData = new UniformData();
            uniformData.name = name;

            if (type.equals("sampler2D")) {
                String body = uniformElement.getText();
                uniformData.type = Type.TEXTURE;
                uniformData.payload = body;
            } else {
                uniformData.type = Type.getFor(type);
            }

            uniformMap.put(name, uniformData);
        }
    }

    public ObjectMap<String, UniformData> getUniformMap () {
        return uniformMap;
    }

    public String getCustomMethods () {
        return customMethods;
    }

    public String getShaderLogic () {
        return fragResolve;
    }

    public String getCustomUniforms () {
        String uniformString = "";
        for(UniformData uniformData: uniformMap.values()) {
            String line = "uniform " + uniformData.type.getTypeString() + " " + uniformData.name + ";";
            uniformString += line + "\n";
        }

        return uniformString;
    }

    public String getFragCode () {
        String string = ShaderBuilder.compileShaderString(this, ShaderBuilder.DEFAULT_TEMPLATE());

        return string;
    }

}
