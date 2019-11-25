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

package com.talosvfx.talos.editor.wrappers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.talosvfx.talos.runtime.Expression;
import com.talosvfx.talos.runtime.modules.MathModule;
import com.talosvfx.talos.runtime.utils.MathExpressionMappings;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class MathModuleWrapper extends ModuleWrapper<MathModule> {

    private VisTextField aField;
    private VisTextField bField;

    VisSelectBox<String> selectBox;

    public MathModuleWrapper() {
        super();
    }

    @Override
    protected float reportPrefWidth() {
        return 180;
    }

    @Override
    public void attachModuleToMyOutput(ModuleWrapper moduleWrapper, int mySlot, int targetSlot) {
        super.attachModuleToMyOutput(moduleWrapper, mySlot, targetSlot);

        module.a.setFlavour(module.output.getFlavour());
        module.b.setFlavour(module.output.getFlavour());
    }

    @Override
    public void setSlotInactive(int slotTo, boolean isInput) {
        super.setSlotInactive(slotTo, isInput);
        if(!isInput) {
            module.a.setFlavour(NumericalValue.Flavour.REGULAR);
            module.b.setFlavour(NumericalValue.Flavour.REGULAR);
        }
    }

    @Override
    public void setModule(MathModule module) {
        super.setModule(module);
        aField.setText(module.getDefaultA() + "");
        bField.setText(module.getDefaultB() + "");
    }

    @Override
    protected void configureSlots() {
        Array<String> mathsExpressions = new Array<>();
        MathExpressionMappings.getAvailableMathExpressions(mathsExpressions);

        selectBox = new VisSelectBox();
        selectBox.setItems(mathsExpressions);

        aField = addInputSlotWithTextField("A: ", MathModule.A);
        leftWrapper.add(selectBox).left().expandX().pad(5).padLeft(17).row();
        bField = addInputSlotWithTextField("B: ", MathModule.B);

        aField.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                float a = floatFromText(aField);
                module.setA(a);
            }
        });

        bField.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                float b = floatFromText(bField);
                module.setB(b);
            }
        });


        addOutputSlot("result", 0);

        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedString = selectBox.getSelected();
                Expression expression = MathExpressionMappings.getMathExpressionForName(selectedString);

                module.setExpression(expression);
            }
        });
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        selectBox.setSelected(MathExpressionMappings.getNameForMathExpression(module.getExpression()));

        aField.setText(module.getDefaultA() + "");
        bField.setText(module.getDefaultB() + "");
    }
}
