package com.rockbite.tools.talos.runtime.modules;


import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.rockbite.tools.talos.editor.script.ScriptCompiler;
import com.rockbite.tools.talos.runtime.scripts.SimpleReturnScript;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class ScriptModule extends Module {

    public static final int INPUT1 = 0;
    public static final int INPUT2 = 1;
    public static final int INPUT3 = 2;
    public static final int INPUT4 = 3;
    public static final int INPUT5 = 4;

    public static final int OUTPUT = 0;

    NumericalValue input1;
    NumericalValue input2;
    NumericalValue input3;
    NumericalValue input4;
    NumericalValue input5;


    String script;

    NumericalValue output;

    SimpleReturnScript returnScript;

    @Override
    protected void defineSlots() {

        script = "return 50;";

        input1 = createInputSlot(INPUT1);
        input2 = createInputSlot(INPUT2);
        input3 = createInputSlot(INPUT3);
        input4 = createInputSlot(INPUT4);
        input5 = createInputSlot(INPUT5);

        output = createOutputSlot(OUTPUT);
    }

    @Override
    public void processValues() {
        if (returnScript != null) {
            output.set(returnScript.evaulate(input1.getFloat(), input2.getFloat(), input3.getFloat(), input4.getFloat(), input5.getFloat()));
        } else {
            output.set(1);
        }
    }

    public void setScript (String script) {
        this.script = script;
        SimpleReturnScript scriptInstance = ScriptCompiler.instance().compile(template.replace(replace, script));

        if (scriptInstance != null) {
            this.returnScript = scriptInstance;
        }
    }

    private static final String replace = "%SCRIPT%";
    private static final String template =
        "import com.rockbite.tools.talos.runtime.scripts.SimpleReturnScript;"
        + "public class SimpleRunIm extends SimpleReturnScript {"
        + "public float evaulate (float i1, float i2, float i3, float i4, float i5) {"
        + "%SCRIPT%"
        + "}"
        + "}";

}
