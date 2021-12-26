package com.talosvfx.talos.editor.addons.shader.nodes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.WorkplaceStage;
import com.talosvfx.talos.editor.addons.shader.ShaderProject;
import com.talosvfx.talos.editor.addons.shader.workspace.ShaderNodeStage;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.nodes.widgets.ValueWidget;
import com.talosvfx.talos.editor.notifications.FileActorBinder;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.NodeCreatedEvent;
import com.talosvfx.talos.editor.notifications.events.NodeDataModifiedEvent;
import com.talosvfx.talos.runtime.shaders.ShaderBuilder;
import com.talosvfx.talos.runtime.utils.ShaderDescriptor;

public class ExternalShaderNode extends AbstractShaderNode {


    public final String OUTPUT = "outputShader";
    private TextArea codeArea;
    private Table customContainer;

    @Override
    public void prepareDeclarations (ShaderBuilder shaderBuilder) {

    }

    @Override
    public String writeOutputCode (String slotId) {
        return codeArea.getText();
    }

    @Override
    protected void updatePreview() {

    }

    @Override
    protected String getPreviewLine (String expression) {
        return null;
    }

    @Override
    public void constructNode (XmlReader.Element module) {
        // add the inputs here
        customContainer = new Table();
        widgetContainer.add(customContainer).growX().row();

        super.constructNode(module);

        codeArea = new TextArea("", getSkin());
        widgetContainer.add(codeArea).padTop(10).padBottom(10).height(200).growX().row();

        FileActorBinder.register(codeArea, ".glsl");

        codeArea.addListener(new FileActorBinder.FileEventListener() {
            @Override
            public void onFileSet (FileHandle fileHandle) {
                String data = fileHandle.readString();

                data = processAnnotations(data);

                codeArea.setText(data);

                updatePreview();
                Notifications.fireEvent(Notifications.obtainEvent(NodeDataModifiedEvent.class).set(ExternalShaderNode.this));
            }
        });

        shaderBox.setVisible(false);


        String defaultText = Gdx.files.internal("addons/shader/shaders/default.frag.glsl").readString();
        defaultText = processAnnotations(defaultText);
        codeArea.setText(defaultText);
    }

    private String processAnnotations (String data) {
        String lines[] = data.split("\n");
        String result = "";
        customContainer.clear(); // todo: also clear maps for uniform inputs
        for(int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            // that is a very specific check we have here.. oh boi
            if (line.startsWith("@") && i < lines.length - 1 && lines[i + 1].trim().startsWith("uniform")) {
                // let's process this annotations
                try {
                    String uniformLine = lines[i + 1].trim();
                    String namePart = uniformLine.split(" ")[2];
                    String uniformName = namePart.substring(0, namePart.length() - 1);

                    // now look for method and arguments

                    String method = line.substring(line.indexOf("[") + 1, line.indexOf("("));
                    String args[] = line.substring(line.indexOf("(") + 1, line.indexOf(")")).split(",");
                    for(int j = 0; j < args.length; j++) {
                        args[j] = args[j].trim();
                    }

                    addUniformInput(uniformName, method, args);

                } catch (Exception e) {
                    System.out.println("error processing this annotation");
                }
            } else {
                result += line + "\n";
            }
        }

        return result;
    }

    private void addUniformInput (String uniformName, String method, String[] args) {
        String xml = "<value port=\"input\" name=\"" + uniformName + "\" type=\"float\">" + uniformName + "</value>";
        XmlReader reader = new XmlReader();
        XmlReader.Element elem = reader.parse(xml);
        addRow(elem, 0, 0, false, customContainer, true);

        String moduleName = "FloatUniform";
        ShaderNodeStage nodeStage = (ShaderNodeStage) TalosMain.Instance().getNodeStage(); // todo: check this cast
        NodeWidget node = nodeStage.createNode(moduleName, this.getX() - 200, this.getY());
        UniformNode uniformNode = (UniformNode) node;
        node.constructNode(nodeStage.getNodeListPopup().getModuleByName(moduleName));
        Notifications.fireEvent(Notifications.obtainEvent(NodeCreatedEvent.class).set(node));
        nodeStage.getNodeBoard().makeConnection(node, this, "out", uniformName);
        ((UniformNode) node).setValue(0.5f);
        uniformNode.setUniformName(uniformName);
    }

    @Override
    protected void showShaderBox () {

    }
}

