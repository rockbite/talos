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
import com.kotcrab.vis.ui.widget.VisTextField;
import com.talosvfx.talos.runtime.vfx.Slot;
import com.talosvfx.talos.runtime.vfx.modules.AbstractModule;
import com.talosvfx.talos.runtime.vfx.modules.HistoryParticlePointDataGeneratorModule;
import com.talosvfx.talos.runtime.vfx.modules.ParticlePointDataGeneratorModule;

public class HistoryParticlePointDataGeneratorModuleWrapper extends ModuleWrapper<HistoryParticlePointDataGeneratorModule> {

    private VisTextField maxPointsTextField;
    private VisTextField minDistanceTextField;


    public HistoryParticlePointDataGeneratorModuleWrapper () {
        super();
    }

    @Override
    public void setModule(HistoryParticlePointDataGeneratorModule module) {
        super.setModule(module);

        maxPointsTextField.setText(module.getMaxPoints() + "");
        minDistanceTextField.setText(module.getMinDistance() + "");
    }

    @Override
    protected float reportPrefWidth() {
        return 250;
    }

    @Override
    protected void configureSlots() {
        addOutputSlot("from-to", ParticlePointDataGeneratorModule.MODULE);


        maxPointsTextField = addInputSlotWithTextField("max points", HistoryParticlePointDataGeneratorModule.POINTS_COUNT);
        maxPointsTextField.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                float numPoints = floatFromText(maxPointsTextField);
                module.setMaxPoints(numPoints);
            }
        });
        maxPointsTextField.setText(HistoryParticlePointDataGeneratorModule.defaultMaxPoints + "");

        minDistanceTextField = addInputSlotWithTextField("min distance", HistoryParticlePointDataGeneratorModule.MIN_DISTANCE);
        minDistanceTextField.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                float numPoints = floatFromText(minDistanceTextField);
                module.setMinDistance(numPoints);
            }
        });
        minDistanceTextField.setText(HistoryParticlePointDataGeneratorModule.defaultMinDistanceBetweenPoints + "");



    }

    @Override
    public Class<? extends AbstractModule> getSlotsPreferredModule (Slot slot) {
        return null;
    }




}
