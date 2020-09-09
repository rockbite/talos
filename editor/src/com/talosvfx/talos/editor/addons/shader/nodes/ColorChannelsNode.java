package com.talosvfx.talos.editor.addons.shader.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.NodeDataModifiedEvent;

public class ColorChannelsNode extends AbstractShaderNode {

    public final String OUTPUT_RGBA = "outputRGBA";

    public final String INPUT_R = "R";
    public final String INPUT_G = "G";
    public final String INPUT_B = "B";
    public final String INPUT_A = "A";

    public ColorChannelsNode (Skin skin) {
        super(skin);
    }

    @Override
    public void constructNode (XmlReader.Element module) {
        super.constructNode(module);
    }

    @Override
    public void prepareDeclarations (ShaderBuilder shaderBuilder) {
        String expR = getExpression(INPUT_R, (float)(widgetMap.get(INPUT_R).getValue())/255f + "");
        String expG = getExpression(INPUT_G, (float)(widgetMap.get(INPUT_G).getValue())/255f + "");
        String expB = getExpression(INPUT_B, (float)(widgetMap.get(INPUT_B).getValue())/255f + "");
        String expA = getExpression(INPUT_A, (float)(widgetMap.get(INPUT_A).getValue())/255f + "");

        shaderBuilder.addLine("vec4 rgbVar" + getId() + " = vec4(" + expR + "," + expG + "," + expB + "," + expA + ")");
    }

    @Override
    public String writeOutputCode (String slotId) {
        return "rgbVar" + getId();
    }

    @Override
    protected String getPreviewOutputName () {
        return OUTPUT_RGBA;
    }

    @Override
    protected String getPreviewLine(String expression) {
        ShaderBuilder.Type outputType = getVarType(getPreviewOutputName());

        expression = castTypes(expression, outputType, ShaderBuilder.Type.VEC4, CAST_STRATEGY_REPEAT);

        return "gl_FragColor = " + expression + ";";
    }
}
