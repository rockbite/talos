package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.utils.IntMap;
import com.rockbite.tools.talos.runtime.values.FloatValue;
import com.rockbite.tools.talos.runtime.values.Value;

public abstract class Module {

    protected ParticleSystem system;

    protected IntMap<Module> inputModules = new IntMap<>(2);
    protected IntMap<Integer> inputSlots = new IntMap<>(2);

    protected IntMap<Value> outputValues = new IntMap<>(2);
    protected IntMap<Value> inputValues;

    protected int index = 1;

    public Module() {
        // must have empty constructor
    }

    public void init(ParticleSystem system) {
        this.system = system;
    }

    protected void createInputSlots(int slotCount) {
        inputModules = new IntMap<>(slotCount);
        inputSlots = new IntMap<>(slotCount);

        inputValues = new IntMap<>(slotCount);
        for(int i = 0; i < slotCount; i++) {
            inputValues.put(i, new FloatValue());
        }
    }

    public void attachModuleToInput(Module module, int inputSlot, int outputSlot) {
        inputModules.put(inputSlot, module);
        inputSlots.put(inputSlot, outputSlot);
    }

    public void detach(Module module) {
        while(true) {
            int slot = inputModules.findKey(module, true, -1);
            if(slot == -1 )break;
            inputModules.remove(slot);
            inputSlots.remove(slot);
        }
    }

    public void attached(Module module, int slot) {

    }

    public void detach(int slot) {
        inputModules.remove(slot);
        inputSlots.remove(slot);
    }

    public boolean isConnectedTo(Module module) {
        if(inputModules.containsValue(module, true)) {
            return true;
        }

        return false;
    }

    /**
     * Need to keep the output values updated
     */
    public abstract void processValues(ScopePayload scopePayload);

    /**
     * Fetches value into particular input slot
     * @param value
     * @param inputSlot
     */
    public void getInputValue(Value value, int inputSlot, ScopePayload scopePayload) {
        if(!inputSlots.containsKey(inputSlot)) {
            value.set(0);
            value.setEmpty(true);
            // fetch default variable which is currently 0
            return;
        }

        value.setEmpty(false);
        int connectedSlot = inputSlots.get(inputSlot);
        Module connectedModule = inputModules.get(inputSlot);

        connectedModule.getOutputValue(value, connectedSlot, scopePayload);
    }

    public void getOutputValue(Value value, int outputSlot, ScopePayload scopePayload) {
        if(outputValues.get(outputSlot) == null) return;
        processValues(scopePayload);
        value.set(outputValues.get(outputSlot));
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
