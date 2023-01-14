package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.misc;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.ObjectMap;

import java.lang.reflect.Field;

public class InterpolationLibrary {

    public static ObjectMap<String, Interpolation> map = new ObjectMap<>();

    public static Interpolation get(String name) {
        if(map.isEmpty()) {
            Field[] declaredFields = Interpolation.class.getDeclaredFields();
            for(Field field: declaredFields) {
                try {
                    map.put(field.getName(), (Interpolation) field.get(null));
                } catch (Exception e) {

                }
            }
        }

        if(name == null || !map.containsKey(name)) {
            return Interpolation.linear;
        }

        return map.get(name);
    }

    private static Interpolation parseInterpolation(String name) {
        Field[] declaredFields = Interpolation.class.getDeclaredFields();
        for(Field field: declaredFields) {
            if(field.getName().equals(name)) {
                try {
                    return (Interpolation) field.get(null);
                } catch (Exception e) {
                    return Interpolation.linear;
                }
            }
        }

        return Interpolation.linear;
    }
}
