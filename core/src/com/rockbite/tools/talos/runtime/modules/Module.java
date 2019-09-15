package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.utils.IntMap;
import com.rockbite.tools.talos.runtime.ParticleSystem;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.Slot;
import com.rockbite.tools.talos.runtime.values.FloatValue;
import com.rockbite.tools.talos.runtime.values.Value;
import com.sun.org.apache.xpath.internal.operations.Variable;

public abstract class Module {

    protected ParticleSystem system;

    protected IntMap<Slot> inputSlots = new IntMap<>();
    protected IntMap<Slot> outputSlots = new IntMap<>();

    protected int index = 1;

    public Module() {
        // must have empty constructor
    }

    public void init(ParticleSystem system) {
        this.system = system;
        defineSlots();
    }

    protected abstract void defineSlots();

    public void attachModuleToMyInput(Module module, int mySlot, int targetSlot) {
        inputSlots.get(mySlot).connect(module, module.outputSlots.get(targetSlot));
    }

    public void attachModuleToMyOutput(Module module, int mySlot, int targetSlot) {
        outputSlots.get(mySlot).connect(module, module.inputSlots.get(targetSlot));
    }

    public void detach(Module module) {
        for(Slot slot : inputSlots.values()) {
            if(slot.getTargetModule() == module) {
                slot.getTargetSlot().detach();
                slot.detach();
            }
        }
    }

    public void detach(int slot) {
        inputSlots.remove(slot);
    }

    public boolean isConnectedTo(Module module) {
        for(Slot slot : inputSlots.values()) {
            if(slot.getTargetModule() == module) {
                return true;
            }
        }

        return false;
    }


    /**
     * Need to keep the output values updated
     */
    public abstract void processValues();


    public <T extends Value> T getValueFromInputSlot(int slotId, Class<T> clazz) {
        return inputSlots.get(slotId).fetchValue(clazz);
    }


    public Value getOutputContainer(int slotId) {
        outputSlots.get(slotId).getValue()
    }


/*

    public void getValue(int outputSlot) {
        Value value = inputValues.get(inputSlot);

        if(!inputSlots.containsKey(inputSlot)) {
            value.set(0);
            value.setEmpty(true);
            // fetch default variable which is currently 0
            return;
        }

        value.setEmpty(false);
        int connectedSlot = inputSlots.get(inputSlot);
        Module connectedModule = inputModules.get(inputSlot);

        connectedModule.getOutputValue(connectedSlot, scopePayload);
    }

    public void getOutputValue(int outputSlot, ScopePayload scopePayload) {
        if(outputValues.get(outputSlot) == null) return;
        processValues(scopePayload);
    }

    public Value getInput(int slot) {
        return inputValues.get(slot);
    }*/

    public void setIndex(int index) {
        this.index = index;
    }


    protected <T extends Value> T createInputSlot(Module module, int index, Class<T> type) {
        return (T) createInputSlot(module, index, new Class[]{type});
    }

    protected <T extends Value> T createInputSlot(Module module, int index, Class<T>[] compatibility) {
        Slot slot = new Slot(module, index, true);
        slot.setCompatibility(compatibility);

        return (T) slot.getValue();
    }

    protected <T extends Value> T createOutputSlot(Module module, int index, Class<T> type) {
        return (T) createInputSlot(module, index, new Class[]{type});
    }

    protected <T extends Value> T createOutputSlot(Module module, int index, Class<T>[] compatibility) {
        Slot slot = new Slot(module, index, false);
        slot.setCompatibility(compatibility);

        return (T) slot.getValue();
    }
}
