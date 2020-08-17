package com.talosvfx.talos.editor.addons.shader.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;
import com.talosvfx.talos.editor.addons.shader.widgets.ShaderBox;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.NodeDataModifiedEvent;

public class ColorNode extends AbstractShaderNode {

    Color color = new Color(Color.CORAL);

    public final int OUTPUT_RGBA = 0;
    public final int OUTPUT_R = 1;
    public final int OUTPUT_G = 2;
    public final int OUTPUT_B = 3;
    public final int OUTPUT_A = 4;

    @Override
    public void prepareDeclarations(ShaderBuilder shaderBuilder) {

    }

    @Override
    public String writeOutputCode(String slotId) {
        return null;
    }
/*
    @Override
    protected void configureConnections () {
        addConnection("RGBA", OUTPUT_RGBA, Align.right);
        addConnection("R", OUTPUT_R, Align.right);
        addConnection("G", OUTPUT_G, Align.right);
        addConnection("B", OUTPUT_B, Align.right);
        addConnection("A", OUTPUT_A, Align.right);
    }

    @Override
    protected void buildContent () {
        shaderBox = new ShaderBox();
        dynamicContentTable.add(shaderBox).padTop(0).growX().expand().height(115).padRight(45).padLeft(-16);

        shaderBox.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                TalosMain.Instance().UIStage().showColorPicker(new ColorPickerAdapter() {
                    @Override
                    public void changed(Color newColor) {
                        super.changed(newColor);
                        color.set(newColor);
                        updatePreview();
                        Notifications.fireEvent(Notifications.obtainEvent(NodeDataModifiedEvent.class).set(ColorNode.this));
                    }
                });
            }
        });

        updatePreview();
    }*/
/*
    private void updatePreview () {
        //previewOutput(OUTPUT_RGBA);
    }

    public void prepareDeclarations (ShaderBuilder shaderBuilder) {

    }

    public String writeOutputCode (String slotId) {
        if(slotId == OUTPUT_RGBA) {
            return "vec4(" + color.r + ", " + color.g + ", " + color.b + ", " + color.a + ")";
        }



        return null;
    }

 */
}
