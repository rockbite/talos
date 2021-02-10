package com.talosvfx.talos.editor.addons.shader.workspace;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlWriter;
import com.talosvfx.talos.runtime.shaders.ShaderBuilder;
import com.talosvfx.talos.editor.addons.shader.nodes.ColorOutput;
import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.NodeRemovedEvent;

import java.io.IOException;
import java.io.StringWriter;

public class ShaderNodeStage extends DynamicNodeStage implements Notifications.Observer {

    private ColorOutput colorOutput;

    public ShaderNodeStage (Skin skin) {
        super(skin);

        Notifications.registerObserver(this);
    }

    @Override
    protected XmlReader.Element loadData () {
        FileHandle list = Gdx.files.internal("addons/shader/nodes.xml");
        XmlReader xmlReader = new XmlReader();
        XmlReader.Element root = xmlReader.parse(list);

        return root;
    }

    @Override
    public NodeWidget createNode (String nodeName, float x, float y) {

        if(!nodeName.equals("ColorOutput")) {
            return super.createNode(nodeName, x, y);
        } else {
            if(colorOutput == null) {
                NodeWidget node = super.createNode(nodeName, x, y);
                colorOutput = (ColorOutput) node;
                return node;
            }
        }

        return null;
    }

    @Override
    public void reset () {
        super.reset();
        colorOutput = null;
    }

    @EventHandler
    public void onNodeRemoved(NodeRemovedEvent event) {
       if (event.getNode() == colorOutput) {
           colorOutput = null;
       }
    }

    public String getFragShader() {
        ShaderBuilder builder = new ShaderBuilder();

        if(colorOutput == null) return "";

        colorOutput.buildFragmentShader(builder);

        return builder.getFragmentString();
    }

    public String getShaderData() {
        ShaderBuilder builder = new ShaderBuilder();

        if(colorOutput == null) return "";

        colorOutput.buildFragmentShader(builder);

        String methods = builder.generateMethods();
        String main = builder.getMainContent();

        StringWriter writer = new StringWriter();
        XmlWriter xml = new XmlWriter(writer);

        try {
            XmlWriter shader = xml.element("shader");
            XmlWriter uniforms = shader.element("uniforms");

            ObjectMap<String, ShaderBuilder.UniformData> declaredUniforms = builder.getDeclaredUniforms();
            for(String uniformName: declaredUniforms.keys()) {
                XmlWriter uniform = uniforms.element("uniform");

                uniform.attribute("name", uniformName);
                uniform.attribute("type", declaredUniforms.get(uniformName).type.getTypeString());
                ShaderBuilder.UniformData uniformData = declaredUniforms.get(uniformName);

                if(uniformData.type == ShaderBuilder.Type.TEXTURE) {
                    uniform.text(uniformData.payload.getValueDescriptor());
                }

                uniform.pop();
            }
            uniforms.pop();
            XmlWriter methodsElem = shader.element("methods");
            methodsElem.text("<![CDATA[" + methods + "]]>");
            methodsElem.pop();
            XmlWriter mainElem = shader.element("main");
            mainElem.text("<![CDATA[" + main + "]]>");
            mainElem.pop();
            shader.pop();

            return writer.toString();
        } catch (IOException e) {
            e.printStackTrace();

            return "";
        }
    }
}
