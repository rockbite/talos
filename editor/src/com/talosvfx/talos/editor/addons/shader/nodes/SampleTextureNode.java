package com.talosvfx.talos.editor.addons.shader.nodes;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Align;
import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;
import com.talosvfx.talos.editor.addons.shader.widgets.ShaderBox;
import com.talosvfx.talos.editor.notifications.FileActorBinder;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.NodeDataModifiedEvent;

public class SampleTextureNode extends AbstractShaderNode {

    private Texture texture;

    public final int INPUT_UV = 0;

    public final int OUTPUT_RGBA = 0;
    public final int OUTPUT_R = 1;
    public final int OUTPUT_G = 2;
    public final int OUTPUT_B = 3;
    public final int OUTPUT_A = 4;

    @Override
    protected void configureConnections () {
        addConnection("UV", 0, Align.left);

        addConnection("RGBA", 0, Align.right);
        addConnection("R", 1, Align.right);
        addConnection("R", 2, Align.right);
        addConnection("B", 3, Align.right);
        addConnection("A", 4, Align.right);
    }

    @Override
    protected void buildContent () {
        shaderBox = new ShaderBox();
        rightTable.add(shaderBox).padTop(5).growX().expand().height(150).padRight(-16).padLeft(-16);


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

    private void updatePreview () {
        previewOutput(OUTPUT_RGBA);
    }

    @Override
    public void prepareDeclarations (ShaderBuilder shaderBuilder) {
        shaderBuilder.declareUniform("u_texture" + getId(), ShaderBuilder.Type.TEXTURE, texture);
    }

    @Override
    public String writeOutputCode (int slotId) {
        String output = "";

        if(slotId == OUTPUT_RGBA) {
            output = "texture2D(" + "u_texture" + getId() + ", v_texCoords)";
        }

        return output;
    }
}
