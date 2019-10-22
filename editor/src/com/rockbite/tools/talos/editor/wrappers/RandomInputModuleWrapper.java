package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.rockbite.tools.talos.runtime.Slot;
import com.rockbite.tools.talos.runtime.modules.RandomInputModule;
import com.rockbite.tools.talos.runtime.values.Value;

public class RandomInputModuleWrapper extends ModuleWrapper<RandomInputModule> {

    Class valueType = null;

    @Override
    protected float reportPrefWidth() {
        return 180;
    }

    @Override
    public void attachModuleToMyInput(ModuleWrapper moduleWrapper, int mySlot, int targetSlot) {
        super.attachModuleToMyInput(moduleWrapper, mySlot, targetSlot);

        addNewInputSlot();

        invalidateHierarchy();
        pack();

        // let's figure out the type
        if(valueType == null) {
            valueType = moduleWrapper.getModule().getOutputSlot(targetSlot).getValue().getClass();
        } else {
            Class newValueType = moduleWrapper.getModule().getOutputSlot(targetSlot).getValue().getClass();
            if(valueType != newValueType) {
                // changing value detaching all previous values
                // detach code goes here
                valueType = newValueType;
            }
        }
        // re init all previous values
        try {
            for(Slot slot : module.getInputSlots().values()) {
                    slot.setValue((Value) ClassReflection.newInstance(valueType));
            }
            module.getOutputSlot(0).setValue((Value) ClassReflection.newInstance(valueType));
        } catch (ReflectionException e) {
            e.printStackTrace();
        }
    }

    private void loadSlots() {
        if(module != null) {
            leftWrapper.clearChildren();
            IntMap<Slot> list = module.getInputSlots();
            int tmpCount = list.size;
            module.slotCount = 0;
            for (int i = 0; i < tmpCount; i++) {
                module.slotCount++;
                addNewInputSlot();
            }

            invalidateHierarchy();
            pack();
        }
    }

    @Override
    public void setSlotInactive(int slotTo, boolean isInput) {
        super.setSlotInactive(slotTo, isInput);
        if(isInput) {
            removeSlot(slotTo, true);
        }
    }

    protected void removeSlot(int slot, boolean isInput) {

        IntMap<Slot> list = module.getInputSlots();
        if(!isInput) {
            list = module.getOutputSlots();
        }
        //remove this slot from module
        for(int i = slot; i >= 0; i++) { // don't do this kids
            if(list.get(i+1) == null) {
                list.remove(i);
                break;
            }
            list.put(i,list.get(i+1));
            list.get(i).setIndex(i);
        }
        module.slotCount--;

        loadSlots();
    }

    private void addNewInputSlot() {
        addInputSlot((module.slotCount) + ": ", module.slotCount-1);
    }


    @Override
    public void setModule(RandomInputModule module) {
        super.setModule(module);
        loadSlots();
    }

    @Override
    protected void configureSlots() {
        addOutputSlot("output", 0);
        loadSlots();
    }
}
