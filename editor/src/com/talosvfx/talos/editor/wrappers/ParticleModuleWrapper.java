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

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.talosvfx.talos.runtime.ParticleDrawable;
import com.talosvfx.talos.runtime.Slot;
import com.talosvfx.talos.runtime.modules.*;
import com.talosvfx.talos.runtime.modules.AbstractModule;
import com.talosvfx.talos.runtime.render.drawables.TextureRegionDrawable;

public class ParticleModuleWrapper extends ModuleWrapper<ParticleModule> {

    ParticleDrawable defaultDrawable;

    public ParticleModuleWrapper() {
        super();
        defaultDrawable = new TextureRegionDrawable(new Sprite(new Texture("fire.png")));
    }

    @Override
    public void setModule(ParticleModule module) {
        super.setModule(module);
        module.setDefaultDrawable(defaultDrawable);
    }

    @Override
    protected float reportPrefWidth() {
        return 150;
    }

    @Override
    protected void configureSlots() {
        addInputSlot("drawable",  ParticleModule.DRAWABLE);
        addInputSlot("offset",  ParticleModule.OFFSET);
        addInputSlot("life",  ParticleModule.LIFE);

        addSeparator(true);

        addInputSlot("velocity",  ParticleModule.VELOCITY);
        addInputSlot("gravity",  ParticleModule.GRAVITY);
        addInputSlot("target",  ParticleModule.TARGET);
        addInputSlot("color",  ParticleModule.COLOR);
        addInputSlot("transparency",  ParticleModule.TRANSPARENCY);
        addInputSlot("angle",  ParticleModule.ANGLE);
        addInputSlot("mass",  ParticleModule.MASS);

        addInputSlot("rotation",  ParticleModule.ROTATION);
        addInputSlot("size",  ParticleModule.SIZE);
        addInputSlot("position",  ParticleModule.POSITION);
    }

    @Override
    public Class<? extends AbstractModule>  getSlotsPreferredModule(Slot slot) {

        if(slot.getIndex() == ParticleModule.DRAWABLE) return TextureModule.class;
        if(slot.getIndex() == ParticleModule.OFFSET) return Vector2Module.class;
        if(slot.getIndex() == ParticleModule.TARGET) return Vector2Module.class;
        if(slot.getIndex() == ParticleModule.LIFE) return StaticValueModule.class;
        if(slot.getIndex() == ParticleModule.VELOCITY) return DynamicRangeModule.class;
        if(slot.getIndex() == ParticleModule.ROTATION) return DynamicRangeModule.class;
        if(slot.getIndex() == ParticleModule.COLOR) return GradientColorModule.class;
        if(slot.getIndex() == ParticleModule.TRANSPARENCY) return CurveModule.class;
        if(slot.getIndex() == ParticleModule.ANGLE) return DynamicRangeModule.class;
        if(slot.getIndex() == ParticleModule.SIZE) return DynamicRangeModule.class;
        if(slot.getIndex() == ParticleModule.POSITION) return Vector2Module.class;

        return null;
    }


}
