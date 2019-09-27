package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.utils.ObjectMap;
import com.rockbite.tools.talos.runtime.modules.Module;

public class WrapperRegistry<T extends Module, U extends ModuleWrapper<T>> {

    public static ObjectMap<Class, Class> map = new ObjectMap<>();

    public static <T extends Module, U extends ModuleWrapper<T>> Class<U> get (Class<T> moduleClass) {
        return map.get(moduleClass);
    }

    public static <T extends Module, U extends ModuleWrapper<T>> void reg(Class<T> moduleClass, Class<U> wrapperClass) {
        map.put(moduleClass, wrapperClass);
    }
}
