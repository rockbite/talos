package com.talosvfx.talos.editor.addons.shader.nodes;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;
import com.talosvfx.talos.editor.addons.shader.ShaderProject;
import com.talosvfx.talos.editor.addons.shader.widgets.ShaderBox;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.notifications.FileActorBinder;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.NodeDataModifiedEvent;

public class SampleTextureNode extends AbstractShaderNode {

    private Texture texture;
    private String texturePath;

    public final String INPUT_UV_OFFSET = "offsetUV";
    public final String INPUT_UV_MUL = "mulUV";

    public final String WRAP = "wrap";

    public final String TEXTURE_ID = "texture";

    public final String OUTPUT_RGBA = "outputRGBA";

    @Override
    protected String getPreviewOutputName () {
        return OUTPUT_RGBA;
    }

    @Override
    protected void inputStateChanged (boolean isInputDynamic) {
        showShaderBox();
    }

    @Override
    protected boolean isInputDynamic () {
        return true;
    }

    @Override
    public void prepareDeclarations(ShaderBuilder shaderBuilder) {
        shaderBuilder.declareUniform("u_texture" + getId(), ShaderBuilder.Type.TEXTURE, texture);
    }

    @Override
    public String writeOutputCode(String slotId) {
        String output = "";

        String uvOffset = getExpression(INPUT_UV_OFFSET, "vec2(0.0, 0.0)");
        String uvMul = getExpression(INPUT_UV_MUL, "vec2(1.0, 1.0)");

        boolean wrap = (boolean) widgetMap.get(WRAP).getValue();

        String sample = "(v_texCoords - 0.5)/(" + uvMul + ") + 0.5 + " + uvOffset;

        if(wrap) {
            sample = "fract(" + sample + ")";
        }

        if(slotId.equals(OUTPUT_RGBA)) {
            output = "texture2D(" + "u_texture" + getId() + ", " + sample + ")";
        }

        return output;
    }

    @Override
    protected void readProperties(JsonValue properties) {
        texturePath = properties.getString("texture", "");
        FileHandle fileHandle = TalosMain.Instance().ProjectController().findFile(texturePath);

        if(fileHandle != null) {
            FileActorBinder.FileEvent fileEvent = Pools.obtain(FileActorBinder.FileEvent.class);
            fileEvent.setFileHandle(fileHandle);
            shaderBox.fire(fileEvent);
        }
    }

    @Override
    protected void writeProperties(Json json) {
        json.writeValue("texture", texturePath);
    }

    @Override
    public void constructNode (XmlReader.Element module) {
        super.constructNode(module);

        FileActorBinder.register(shaderBox, "png");

        shaderBox.addListener(new FileActorBinder.FileEventListener() {

            @Override
            public void onFileSet (FileHandle fileHandle) {
                try {
                    texture = new Texture(fileHandle);
                    texturePath = fileHandle.path();
                    updatePreview();
                } catch (Exception e) {

                }
            }
        });
    }

    @Override
    protected String getPreviewLine(String expression) {
        ShaderBuilder.Type outputType = getVarType(getPreviewOutputName());

        expression = castTypes(expression, outputType, ShaderBuilder.Type.VEC4, CAST_STRATEGY_REPEAT);

        return "gl_FragColor = " + expression + ";";
    }
}
