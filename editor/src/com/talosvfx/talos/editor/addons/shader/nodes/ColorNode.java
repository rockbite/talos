package com.talosvfx.talos.editor.addons.shader.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.runtime.shaders.ShaderBuilder;
import com.talosvfx.talos.editor.nodes.widgets.ColorWidget;

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
            }
        });
    }

    @Override
    protected String getPreviewOutputName () {
        return OUTPUT_RGBA;
    }

    @Override
    public String writeOutputCode(String slotId) {
        String expression = getExpression(INPUT_COLOR, null);

        expression = "(" + expression + ")";

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

    public void prepareDeclarations (ShaderBuilder shaderBuilder) {

    }

    @Override
    protected String getPreviewLine(String expression) {
        ShaderBuilder.Type outputType = getVarType(getPreviewOutputName());

        expression = castTypes(expression, outputType, ShaderBuilder.Type.VEC4, CAST_STRATEGY_REPEAT);

        return "return " + expression;
    }
}
