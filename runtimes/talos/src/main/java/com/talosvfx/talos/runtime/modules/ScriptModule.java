/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.talosvfx.talos.runtime.modules;


import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.script.ScriptCompiler;
import com.talosvfx.talos.runtime.scripts.SimpleReturnScript;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class ScriptModule extends AbstractModule {

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
    public void processCustomValues () {
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
//        SimpleReturnScript scriptInstance = ScriptCompiler.instance().compile(template.replace(replace, script));

//        if (scriptInstance != null) {
//            this.returnScript = scriptInstance;
//        }
    }

    private static final String replace = "%SCRIPT%";
    private static final String template =
        "import com.talosvfx.talos.runtime.scripts.SimpleReturnScript;"
        + "import com.talosvfx.talos.runtime.values.NumericalValue;"
        + "public class SimpleRunIm extends SimpleReturnScript {"
        + "public void evaulate (NumericalValue i1, NumericalValue i2, NumericalValue i3, NumericalValue i4, NumericalValue i5, NumericalValue o1, NumericalValue o2, NumericalValue o3, NumericalValue o4, NumericalValue o5) {"
        + "%SCRIPT%"
        + "}"
        + "}";

    public String getScript () {
        return script;
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("script", script);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        this.script = jsonData.getString("script");
    }

}
