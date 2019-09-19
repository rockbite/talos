package com.rockbite.tools.talos.runtime.modules;


import com.rockbite.tools.talos.runtime.script.ScriptCompiler;
import com.rockbite.tools.talos.runtime.scripts.SimpleReturnScript;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class ScriptModule extends Module {

    public static final int INPUT1 = 0;
    public static final int INPUT2 = 1;
    public static final int INPUT3 = 2;
    public static final int INPUT4 = 3;
    public static final int INPUT5 = 4;

    public static final int OUTPUT1 = 0;
    public static final int OUTPUT2 = 1;
    public static final int OUTPUT3 = 2;
    public static final int OUTPUT4 = 3;
    public static final int OUTPUT5 = 4;


    NumericalValue input1;
    NumericalValue input2;
    NumericalValue input3;
    NumericalValue input4;
    NumericalValue input5;


    String script;

    NumericalValue output1;
    NumericalValue output2;
    NumericalValue output3;
    NumericalValue output4;
    NumericalValue output5;

    SimpleReturnScript returnScript;

    @Override
    protected void defineSlots() {

        script = "return 50;";

        input1 = createInputSlot(INPUT1);
        input2 = createInputSlot(INPUT2);
        input3 = createInputSlot(INPUT3);
        input4 = createInputSlot(INPUT4);
        input5 = createInputSlot(INPUT5);

        output1 = createOutputSlot(OUTPUT1);
        output2 = createOutputSlot(OUTPUT2);
        output3 = createOutputSlot(OUTPUT3);
        output4 = createOutputSlot(OUTPUT4);
        output5 = createOutputSlot(OUTPUT5);
    }

    @Override
    public void processValues() {
        if (returnScript != null) {
            returnScript.evaulate(input1, input2, input3, input4, input5, output1, output2, output3, output4, output5);
        } else {
            output1.set(1);
            output2.set(1);
            output3.set(1);
            output4.set(1);
            output1.set(1);
            output5.set(1);
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
        + "import com.rockbite.tools.talos.runtime.values.NumericalValue;"
        + "public class SimpleRunIm extends SimpleReturnScript {"
        + "public void evaulate (NumericalValue i1, NumericalValue i2, NumericalValue i3, NumericalValue i4, NumericalValue i5, NumericalValue o1, NumericalValue o2, NumericalValue o3, NumericalValue o4, NumericalValue o5) {"
        + "%SCRIPT%"
        + "}"
        + "}";

}
