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

package com.rockbite.tools.talos.editor.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class DynamicFloatRangeInputWidget extends Table {

    private CurveWidget curveWidget;

    private FloatRangeInputWidget lowInput;
    private FloatRangeInputWidget highInput;

    public DynamicFloatRangeInputWidget(Skin skin) {
       setSkin(skin);

        Table container = new Table();

        highInput = new FloatRangeInputWidget("HMin", "HMax", getSkin());
        lowInput = new FloatRangeInputWidget("LMin", "LMax", getSkin());

        lowInput.setValue(0, 0);
        highInput.setValue(1, 1);

        container.add(highInput).row();
        container.add().height(3).row();
        container.add(lowInput);

        add(container).left().expandX();

        curveWidget = new CurveWidget(getSkin());
        add(curveWidget).left().growY().width(100).padTop(23).padRight(3).padLeft(4).padBottom(3);
    }

    public void setFlavour(NumericalValue.Flavour flavour) {
        lowInput.setFlavour(flavour);
        highInput.setFlavour(flavour);
    }
}
