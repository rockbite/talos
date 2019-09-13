package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.utils.ObjectMap;

public class WrapperRegistry {

    public static ObjectMap<Class, Class> map = new ObjectMap<>();

    public static void reg(Class moduleClass, Class wrapperClass) {
        map.put(moduleClass, wrapperClass);
    }
}
