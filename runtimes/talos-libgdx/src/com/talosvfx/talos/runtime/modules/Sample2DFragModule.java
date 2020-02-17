package com.talosvfx.talos.runtime.modules;

import com.talosvfx.talos.runtime.values.DrawableValue;
import com.talosvfx.talos.runtime.values.NumericalValue;
import com.talosvfx.talos.runtime.values.StringValue;
import com.talosvfx.talos.runtime.values.Value;

public class Sample2DFragModule extends AbstractModule {

    public static final int TEXTURE = 0;
    public static final int POSITION = 1;

    public static final int RGBA = 0;
    public static final int R = 1;
    public static final int G = 2;
    public static final int B = 3;
    public static final int A = 4;

    private DrawableValue texture;
    private NumericalValue position;

    private StringValue rgba;
    private StringValue r;
    private StringValue g;
    private StringValue b;
    private StringValue a;

    @Override
    protected void defineSlots() {
        texture = (DrawableValue) createInputSlot(TEXTURE, new DrawableValue());
        position = createInputSlot(POSITION);

        rgba = (StringValue) createOutputSlot(RGBA, new StringValue());
        r =    (StringValue) createOutputSlot(R, new StringValue());
        g =    (StringValue) createOutputSlot(G, new StringValue());
        b =    (StringValue) createOutputSlot(B, new StringValue());
        a =    (StringValue) createOutputSlot(A, new StringValue());
    }

    @Override
    public Value fetchOutputSlotValue(int slotId) {

        fetchOutputShaderCode();

        processValues();
        return outputSlots.get(slotId).getValue();
    }

    @Override
    public void processValues() {

    }

    @Override
    public void processShaderCode() {
        String result = "vec2 offset = vec2(0.0, 0.0);";
        result += shaderCode;
        result += "vec2 coords = v_texCoords + offset;";

        String rGBACode = "color = texture2D(u_texture, coords).rgba;";
        String rCode    = "color = vec4(texture2D(u_texture, coords).r);";
        String gCode    = "color = vec4(texture2D(u_texture, coords).g);";
        String bCode    = "color = vec4(texture2D(u_texture, coords).b);";
        String aCode    = "color = vec4(texture2D(u_texture, coords).a);";

        rgba.set(result + rGBACode);
        r.set (result + rCode);
        g.set (result + gCode);
        b.set (result + bCode);
        a.set (result + aCode);
    }
}
