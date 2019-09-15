package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.rockbite.tools.talos.runtime.modules.Module;
import com.rockbite.tools.talos.runtime.values.Value;

public class Slot {

    private int index;
    private boolean isInput;
    private Flavour flavour;

    private Module currentModule;
    private Module targetModule;
    private Slot targetSlot;

    private ObjectMap<Class<? extends Value>, Value> valueObjects;

    private Class<? extends Value> resolvedType;

    public Value val(Class<? extends Value> clazz) {
        return valueObjects.get(clazz);
    }

    public Module getTargetModule() {
        return targetModule;
    }

    public <T extends Value> T getValue(Class<T> clazz) {
        return (T) valueObjects.get(resolvedType);
    }

    public <T extends Value> T fetchValue(Class<T> clazz) {
        if(isInput) {
            valueObjects.get(resolvedType).set(targetSlot.fetchValue(clazz));
            return getValue(clazz);
        } else {
            currentModule.processValues();
            return getValue(clazz);
        }
    }

    enum Flavour {
        ANGLE
    }

    public Slot(Module currentModule, int index, boolean isInput) {
        this.currentModule = currentModule;
        this.index = index;
        this.isInput = isInput;
    }

    public <T extends Value> void setCompatibility(Class<T>[] arr) {
        valueObjects = new ObjectMap<>(arr.length);
        for(int i = 0; i < arr.length; i++) {
            try {
                Value value = ClassReflection.newInstance(arr[i]);
                valueObjects.put(arr[i], value);
            } catch (ReflectionException e) {
                e.printStackTrace();
            }
        }
    }

    public void connect(Module targetModule, Slot targetSlot) {
        this.targetModule = targetModule;
        this.targetSlot = targetSlot;
    }

    public Slot getTargetSlot() {
        return targetSlot;
    }


    public void detach() {
        this.targetModule = null;
        this.targetSlot = null;
    }

}
