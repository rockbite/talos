package com.talosvfx.talos.runtime.scene.utils.propertyWrappers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.runtime.scene.GameObject;

class PropertyWrappers {
    private final ObjectMap<Class, Class<? extends PropertyWrapper<?>>> registeredTypes = new ObjectMap<>();

    private ObjectMap<String, String> primitiveReplacementMap = new ObjectMap<>();

    PropertyWrappers () {
        primitiveReplacementMap.put("float", Float.class.getName());
        primitiveReplacementMap.put("int", Integer.class.getName());
        primitiveReplacementMap.put("boolean", Boolean.class.getName());
        primitiveReplacementMap.put("GameObject", GameObject.class.getName());
        primitiveReplacementMap.put("String", String.class.getName());
        primitiveReplacementMap.put("vec2", Vector2.class.getName());

        registerSupportedClasses();
    }

    private void registerSupportedClasses () {
        registerPropertyWrapper(Float.class, PropertyFloatWrapper.class);
        registerPropertyWrapper(Boolean.class, PropertyBooleanWrapper.class);
        registerPropertyWrapper(Integer.class, PropertyIntegerWrapper.class);
        registerPropertyWrapper(String.class, PropertyStringWrapper.class);
        registerPropertyWrapper(GameObject.class, PropertyGameObjectWrapper.class);
        registerPropertyWrapper(Vector2.class, PropertyVec2Wrapper.class);
    }

    <T> void registerPropertyWrapper (Class<T> clazz, Class<? extends PropertyWrapper<T>> wrapperClazz) {
        this.registeredTypes.put(clazz, wrapperClazz);
    }

    String parseName (String className) {
        if (primitiveReplacementMap.containsKey(className)) {
            className = primitiveReplacementMap.get(className);
        }
        return className;
    }

    boolean supportsProperty (String property) {
        property = parseName(property);
        try {
            Class classForName = ClassReflection.forName(property);
            return registeredTypes.containsKey(classForName);
        } catch (ReflectionException e) {
            e.printStackTrace();
            return false;
        }

    }

    @SuppressWarnings("unchecked")
    <T> PropertyWrapper<T> createPropertyWrapperForClazz (Class<T> clazz) {
        Class<PropertyWrapper<T>> aClass = (Class<PropertyWrapper<T>>)registeredTypes.get(clazz);
        try {
            return ClassReflection.newInstance(aClass);
        } catch (ReflectionException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> PropertyWrapper<T> createPropertyWrapperForClazzName (String parameterClassName) {
        String clazzName = parseName(parameterClassName);
        Class aClass = null;
        try {
            aClass = ClassReflection.forName(clazzName);
        } catch (ReflectionException e) {
            throw new RuntimeException(e);
        }
        return createPropertyWrapperForClazz(aClass);
    }

    public Array<String> getPrimitiveTypeNames () {
        return primitiveReplacementMap.keys().toArray();
    }
}
