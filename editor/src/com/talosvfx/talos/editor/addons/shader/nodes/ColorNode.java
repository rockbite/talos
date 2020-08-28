package com.talosvfx.talos.editor.addons.shader.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.XmlReader;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;
import com.talosvfx.talos.editor.addons.shader.widgets.ShaderBox;
import com.talosvfx.talos.editor.nodes.NodeBoard;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.nodes.widgets.ColorWidget;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.NodeDataModifiedEvent;

public class ColorNode extends AbstractShaderNode {

    Color color = new Color(Color.CORAL);

    public final String OUTPUT_RGBA = "outputRGBA";
    public final String OUTPUT_R = "outputR";
    public final String OUTPUT_G = "outputG";
    public final String OUTPUT_B = "outputB";
    public final String OUTPUT_A = "outputA";

    public final String INPUT_COLOR = "color";

    @Override
    public void constructNode (XmlReader.Element module) {
        super.constructNode(module);

        widgetMap.get(INPUT_COLOR).addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent changeEvent, Actor actor) {
                color.set(((ColorWidget)(widgetMap.get(INPUT_COLOR))).getValue());
                Notifications.fireEvent(Notifications.obtainEvent(NodeDataModifiedEvent.class).set(ColorNode.this));
            }
        });
    }

    @Override
    protected String getPreviewOutputName () {
        return OUTPUT_RGBA;
    }

    @Override
    public String writeOutputCode(String slotId) {
        String inputColor = inputStrings.get(INPUT_COLOR);

        String expression = "(vec4(" + color.r + ", " + color.g + ", " + color.b + ", " + color.a + "))";

        if(inputColor != null) {
            expression = "("+inputColor+")";
        }

        if(slotId.equals(OUTPUT_RGBA)) {
            return expression;
        }

        if(slotId.equals(OUTPUT_R)) {
            return expression + ".r";
        }

        if(slotId.equals(OUTPUT_G)) {
            return expression + ".g";
        }

        if(slotId.equals(OUTPUT_B)) {
            return expression + ".b";
        }

        if(slotId.equals(OUTPUT_A)) {
            return expression + ".a";
        }

        return null;
    }

    @Override
    public void act (float delta) {
        super.act(delta);

        if(shaderBox != null && shaderBox.isVisible()) {
            shaderBox.setShader(previewBuilder);
        }
    }

    public void prepareDeclarations (ShaderBuilder shaderBuilder) {

    }
}
