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

    public final String OUTPUT_RGBA = "outputRGBA";
    public final String OUTPUT_RGB = "outputRGB";
    public final String OUTPUT_A = "outputAlpha";

    @Override
    public String writeOutputCode(String slotId) {
        if(slotId.equals(OUTPUT_RGBA)) {
            return "vec4(" + color.r + ", " + color.g + ", " + color.b + ", " + color.a + ")";
        }

        return null;
    }

    //Notifications.fireEvent(Notifications.obtainEvent(NodeDataModifiedEvent.class).set(ColorNode.this));

    public void prepareDeclarations (ShaderBuilder shaderBuilder) {

    }
}
