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

import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.runtime.vfx.modules.AbstractModule;

public class WrapperRegistry<T extends AbstractModule, U extends ModuleWrapper<T>> {

    public static ObjectMap<Class, Class> map = new ObjectMap<>();

    public static <T extends AbstractModule, U extends ModuleWrapper<T>> Class<U> get (Class<T> moduleClass) {
        return map.get(moduleClass);
    }

    public static <T extends AbstractModule, U extends ModuleWrapper<T>> void reg(Class<T> moduleClass, Class<U> wrapperClass) {
        map.put(moduleClass, wrapperClass);
    }
}
