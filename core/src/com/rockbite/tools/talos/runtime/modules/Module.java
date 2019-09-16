package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.utils.IntMap;
import com.rockbite.tools.talos.runtime.ParticleSystem;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.Slot;
import com.rockbite.tools.talos.runtime.values.NumericalValue;
import com.rockbite.tools.talos.runtime.values.Value;

public abstract class Module {

    protected ParticleSystem system;

    protected IntMap<Slot> inputSlots = new IntMap<>();
    protected IntMap<Slot> outputSlots = new IntMap<>();

    protected int index = 1;

    private float lastRequester;

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
        inputSlots.get(slot).detach();
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


    public void setIndex(int index) {
        this.index = index;
    }


    /**
     * Fetch value from the input of this module
     * @param slotId
     */
    public void fetchInputSlotValue(int slotId) {
        //find what it is connected to
        Slot inputSlot = inputSlots.get(slotId);

        if(inputSlot.getTargetSlot() == null) {
            inputSlot.getValue().setEmpty(true);
        } else {
            //ask it's module give it's output value
            Value result = inputSlot.getTargetModule().fetchOutputSlotValue(inputSlot.getTargetSlot().getIndex());
            inputSlot.getValue().set(result);
            inputSlot.getValue().setEmpty(false);
        }
    }

    /**
     * this module is asked to calculate and then give it's output value
     * @param slotId
     */
    public Value fetchOutputSlotValue(int slotId) {
        float requester = getScope().get(ScopePayload.REQUESTER_ID).getFloat();

        if(lastRequester != requester) { // caching mechanism
            //fetch all local inputs
            fetchAllInputSlotValues();
            // process
            processValues();
            lastRequester = requester;
        }

        return outputSlots.get(slotId).getValue();
    }

    public void fetchAllInputSlotValues() {
        for(Slot inputSlot : inputSlots.values()) {
            fetchInputSlotValue(inputSlot.getIndex());
        }
    }

    public Value createInputSlot(int slotId, Value value) {
        inputSlots.put(slotId, new Slot(this, slotId, true));
        inputSlots.get(slotId).setValue(value);

        return value;
    }

    public Value createOutputSlot(int slotId, Value value) {
        outputSlots.put(slotId, new Slot(this, slotId, false));
        outputSlots.get(slotId).setValue(value);

        return value;
    }

    public NumericalValue createInputSlot(int slotId) {
        inputSlots.put(slotId, new Slot(this, slotId, true));
        NumericalValue value = new NumericalValue();
        inputSlots.get(slotId).setValue(value);

        return value;
    }

    public NumericalValue createOutputSlot(int slotId) {
        outputSlots.put(slotId, new Slot(this, slotId, false));
        NumericalValue value = new NumericalValue();
        outputSlots.get(slotId).setValue(value);

        return value;
    }

    public ScopePayload getScope() {
        return system.scopePayload;
    }

    public Slot getInputSlot(int slotId) {
        return inputSlots.get(slotId);
    }

    public Slot getOutputSlot(int slotId) {
        return outputSlots.get(slotId);
    }

    public void resetLastRequester() {
        lastRequester = -1f;
    }
}
