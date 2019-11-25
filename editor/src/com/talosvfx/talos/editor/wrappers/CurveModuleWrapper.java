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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.widgets.CurveDataProvider;
import com.talosvfx.talos.editor.widgets.CurveWidget;
import com.talosvfx.talos.runtime.Slot;
import com.talosvfx.talos.runtime.modules.*;
import com.talosvfx.talos.runtime.modules.AbstractModule;

public class CurveModuleWrapper extends ModuleWrapper<CurveModule> implements CurveDataProvider {

    private CurveWidget curveWidget;

    @Override
    protected float reportPrefWidth() {
        return 250;
    }

    @Override
    protected void configureSlots() {

        addInputSlot("alpha (0 to 1)", InterpolationModule.ALPHA);

        addOutputSlot("output", 0);

        curveWidget = new CurveWidget(getSkin());
        contentWrapper.add(curveWidget).expandX().fillX().growX().height(100).padTop(23).padRight(3).padBottom(3);
        curveWidget.setDataProvider(this);

        leftWrapper.add(new Table()).expandY();
        rightWrapper.add(new Table()).expandY();
    }

    @Override
    public Class<? extends AbstractModule> getSlotsPreferredModule(Slot slot) {
        if(slot.getIndex() == CurveModule.ALPHA) return InputModule.class;

        return null;
    }

    @Override
    public Array<Vector2> getPoints() {
        if(module == null) return null;

        return module.getPoints();
    }

    @Override
    public void removePoint(int index) {
        if(module == null) return;
        module.removePoint(index);
    }

    @Override
    public int createPoint(float x, float y) {
        if(module == null) return 0;
        return module.createPoint(x, y);
    }
}
