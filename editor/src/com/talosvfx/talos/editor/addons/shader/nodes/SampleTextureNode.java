package com.talosvfx.talos.editor.addons.shader.nodes;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;
import com.talosvfx.talos.editor.addons.shader.widgets.ShaderBox;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.notifications.FileActorBinder;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.NodeDataModifiedEvent;

public class SampleTextureNode extends AbstractShaderNode {

    private Texture texture;

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
    public void prepareDeclarations(ShaderBuilder shaderBuilder) {
        shaderBuilder.declareUniform("u_texture" + getId(), ShaderBuilder.Type.TEXTURE, texture);
    }

    @Override
    public String writeOutputCode(String slotId) {
        String output = "";

        String uvOffset = getExpression(INPUT_UV_OFFSET, "vec2(0.0, 0.0)");
        String uvMul = getExpression(INPUT_UV_MUL, "vec2(1.0, 1.0)");

        boolean wrap = (boolean) widgetMap.get(WRAP).getValue();

        String sample = "v_texCoords * " + uvMul + " + " + uvOffset;

        if(wrap) {
            sample = "fract(" + sample + ")";
        }

        if(slotId.equals(OUTPUT_RGBA)) {
            output = "texture2D(" + "u_texture" + getId() + ", " + sample + ")";
        }

        return output;
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
                    updatePreview();
                    Notifications.fireEvent(Notifications.obtainEvent(NodeDataModifiedEvent.class).set(SampleTextureNode.this));
                } catch (Exception e) {

                }
            }
        });
    }
}
