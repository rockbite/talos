package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.runtime.ParticleEmitterDescriptor;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.Slot;
import com.rockbite.tools.talos.runtime.values.NumericalValue;
import com.rockbite.tools.talos.runtime.values.Value;

public abstract class Module implements Json.Serializable {

    private ParticleEmitterDescriptor graph;

    protected IntMap<Slot> inputSlots = new IntMap<>();
    protected IntMap<Slot> outputSlots = new IntMap<>();

    static int indexCounter = 0;
    protected int index = indexCounter++;

    private float lastRequester;

    public Module () {
        init();
    }

    protected void init () {
        defineSlots();
    }

    public void setModuleGraph (ParticleEmitterDescriptor graph) {
        this.graph = graph;
    }

    protected abstract void defineSlots();

    public void attachModuleToMyInput(Module module, int mySlot, int targetSlot) {
        if(inputSlots.get(mySlot) == null || module.outputSlots.get(targetSlot) == null) return;
        inputSlots.get(mySlot).connect(module, module.outputSlots.get(targetSlot));
    }

    public void attachModuleToMyOutput(Module module, int mySlot, int targetSlot) {
        if(inputSlots.get(mySlot) == null || module.outputSlots.get(targetSlot) == null) return;
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

    public void detach(int slot, boolean isInput) {
        if(isInput) {
            inputSlots.get(slot).detach();
        }
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
        return graph.getScope();
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

    @Override
    public void write (Json json) {
    }

    @Override
    public void read (Json json, JsonValue jsonData) {

    }
}
