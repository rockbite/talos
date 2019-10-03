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

    public static int inputSlotMethodCall;
    ParticleEmitterDescriptor graph;

    public Slot[] inputSlots = new Slot[20];
    public Slot[] outputSlots = new Slot[20];

    static int indexCounter = 0;
    protected int index = indexCounter++;

    public float lastRequester;

    public static int totalModuleProcessCount = 0;

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
        if(inputSlots[mySlot] == null || module.outputSlots[targetSlot] == null) return;
        inputSlots[mySlot].connect(module, module.outputSlots[targetSlot]);
    }

    public void attachModuleToMyOutput(Module module, int mySlot, int targetSlot) {
        if(inputSlots[mySlot] == null || module.outputSlots[targetSlot] == null) return;
        outputSlots[mySlot].connect(module, module.inputSlots[targetSlot]);
    }

    public void detach(Module module) {
        for (int i = 0; i < inputSlots.length; i++) {
            if (inputSlots[i] == null) continue;
            Slot slot = inputSlots[i];
            if(slot.getTargetModule() == module) {
                slot.getTargetSlot().detach();
                slot.detach();
            }
        }
    }

    public void detach(int slot, boolean isInput) {
        if(isInput) {
            inputSlots[slot].detach();
        }
    }

    public boolean isConnectedTo(Module module) {
        for (int i = 0; i < inputSlots.length; i++) {
            if (inputSlots[i] == null) continue;
            Slot slot = inputSlots[i];
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
        Module.inputSlotMethodCall++;

        //find what it is connected to
        Slot inputSlot = inputSlots[slotId];

        final Value value = inputSlot.value;
        final Slot targetSlot = inputSlot.targetSlot;
        if(targetSlot == null) {
            value.isEmpty = true;
        } else {
            //ask it's module give it's output value
            Value result = inputSlot.targetModule.fetchOutputSlotValue(inputSlot.targetSlot.index);
            value.set(result);
            value.isEmpty = false;
        }
    }

    /**
     * this module is asked to calculate and then give it's output value
     * @param slotId
     */
    public Value fetchOutputSlotValue(int slotId) {
        Module.inputSlotMethodCall++;

        float requester = graph.scopePayload.internalMap[ScopePayload.REQUESTER_ID].elements[0]; //get float

        if(lastRequester != requester) { // caching mechanism
            totalModuleProcessCount++;

            //fetch all local inputs
            fetchAllInputSlotValues();
            // process
            processValues();
            lastRequester = requester;
        }

        return outputSlots[slotId].value;
    }

    public void fetchAllInputSlotValues() {
        for (int i = 0; i < inputSlots.length; i++) {
            if (inputSlots[i] == null) continue;

            fetchInputSlotValue(inputSlots[i].index);
        }
    }

    public Value createInputSlot(int slotId, Value value) {
        inputSlots[slotId] = new Slot(this, slotId, true);
        inputSlots[slotId].setValue(value);

        return value;
    }

    public Value createOutputSlot(int slotId, Value value) {
        outputSlots[slotId] = new Slot(this, slotId, false);
        outputSlots[slotId].setValue(value);

        return value;
    }

    public NumericalValue createInputSlot(int slotId) {
        inputSlots[slotId] = new Slot(this, slotId, true);
        NumericalValue value = new NumericalValue();
        inputSlots[slotId].setValue(value);

        return value;
    }

    public NumericalValue createOutputSlot(int slotId) {
        outputSlots[slotId] = new Slot(this, slotId, false);
        NumericalValue value = new NumericalValue();
        outputSlots[slotId].setValue(value);

        return value;
    }

    public Slot getInputSlot(int slotId) {
        return inputSlots[slotId];
    }

    public Slot getOutputSlot(int slotId) {
        return outputSlots[slotId];
    }

    public void resetLastRequester() {
        lastRequester = -1f;
    }

    @Override
    public void write (Json json) {
        json.writeValue("index", index);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        if(jsonData.has("index")) {
            index = jsonData.getInt("index");
        }
    }

    public int getIndex() {
        return index;
    }
}
