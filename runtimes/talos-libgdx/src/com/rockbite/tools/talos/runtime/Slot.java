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

    private Value value;

    private String text = "";

    public Module getTargetModule() {
        return targetModule;
    }

    public boolean isInput() {
        return isInput;
    }

    public String getText() {
        return text;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    enum Flavour {
        ANGLE
    }

    public Slot(Module currentModule, int index, boolean isInput) {
        this.currentModule = currentModule;
        this.index = index;
        this.isInput = isInput;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public void connect(Module targetModule, Slot targetSlot) {
        this.targetModule = targetModule;
        this.targetSlot = targetSlot;
    }

    public Slot getTargetSlot() {
        return targetSlot;
    }

    public Value getValue() {
        return value;
    }

    public boolean isCompatable(Slot slot) {
        if(value == null || slot.value == null) return true;

        return value.getClass() == slot.value.getClass();
    }

    public void detach() {
        this.targetModule = null;
        this.targetSlot = null;
    }

}
