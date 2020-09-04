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

package com.talosvfx.talos.editor;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.widgets.ui.timeline.TimelineItemDataProvider;
import com.talosvfx.talos.runtime.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.ParticleEmitterDescriptor;

public class ParticleEmitterWrapper implements TimelineItemDataProvider<ParticleEmitterWrapper> {

    private String emitterName = "";
    public boolean isMuted;
    private boolean isSolo;
    private float position;

    private ParticleEmitterDescriptor moduleGraph;

    public ParticleEmitterDescriptor getGraph() {
        return moduleGraph;
    }

    public void setModuleGraph(ParticleEmitterDescriptor graph) {
        this.moduleGraph = graph;
    }

    public String getName() {
        return emitterName;
    }

    public void setName(String emitterName) {
        this.emitterName = emitterName;
    }

    public ParticleEmitterDescriptor getEmitter() {
        return moduleGraph;
    }

    @Override
    public Array<Button> registerSecondaryActionButtons() {
        return null;
    }

    @Override
    public Array<Button> registerMainActionButtons() {
        return null;
    }

    @Override
    public String getItemName() {
        return emitterName;
    }

    public float getPosition() {
        return position;
    }

    public void setPosition(float position) {
        this.position = position;
    }

    @Override
    public ParticleEmitterWrapper getIdentifier() {
        return this;
    }

    @Override
    public int getIndex() {
        return getEmitter().getSortPosition();
    }

    @Override
    public boolean isFull () {
        if(getEmitter().getParticleModule() == null || getEmitter().getEmitterModule() == null) return false;

        return getEmitter().isContinuous();
    }

    @Override
    public float getDurationOne () {
        if(getEmitter().getParticleModule() == null || getEmitter().getEmitterModule() == null) return 0;

        if(getEmitter().getEffectDescriptor().isContinuous() && !getEmitter().isContinuous()) {
            // apparently if effect is continuous, non continuous effect currently don't play
            return 0;
        }

        return getEmitter().getEmitterModule().getDuration();
    }

    @Override
    public float getDurationTwo () {
        if(getEmitter().getParticleModule() == null || getEmitter().getEmitterModule() == null) return 0;

        if(getEmitter().getEffectDescriptor().isContinuous() && !getEmitter().isContinuous()) {
            // apparently if effect is continuous, non continuous effect currently don't play
            return 0;
        }

        if(getEmitter().isContinuous()) {
            return 0;
        }

        return getEmitter().getParticleModule().getLife();
    }

    @Override
    public float getTimePosition () {
        if(getEmitter().getParticleModule() == null || getEmitter().getEmitterModule() == null) return 0;

        return getEmitter().getEmitterModule().getDelay();
    }

    @Override
    public boolean isItemVisible () {
        return !isMuted;
    }

    @Override
    public void setTimePosition (float time) {

    }
}
