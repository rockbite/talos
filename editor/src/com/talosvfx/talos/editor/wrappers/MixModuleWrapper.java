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

import com.talosvfx.talos.runtime.vfx.modules.MixModule;

public class MixModuleWrapper extends ModuleWrapper<MixModule> {

    @Override
    protected float reportPrefWidth() {
        return 180;
    }

    @Override
    protected void configureSlots() {

        addInputSlot("Value One", MixModule.VAL1);
        addInputSlot("mix ratio (0..1)", MixModule.ALPHA);
        addInputSlot("Value Two", MixModule.VAL2);

        addOutputSlot("result", 0);
    }
}
