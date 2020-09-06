package com.talosvfx.talos.runtime.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;

public class ShaderDescriptor {

    public static class UniformData {
        public String name;
        public Type type;
        public String payload;
    }

    public enum Type {
        FLOAT,
        VEC2, VEC3, VEC4,
        TEXTURE
    }

    private ObjectMap<String, UniformData> uniformMap = new ObjectMap<>();

    private String fragCode;

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
        XmlReader.Element code = shader.getChildByName("code");

        fragCode = code.getText();

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
                uniformData.type = Type.FLOAT; //TODO: this we can handle some other time
            }

            uniformMap.put(name, uniformData);
        }
    }

    public ObjectMap<String, UniformData> getUniformMap () {
        return uniformMap;
    }

    public String getFragCode () {
        return fragCode;
    }
}
