package com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.nodes;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.RoutineInstance;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.RoutineNode;

public class ProbabilityChoiceNode extends ARndNode {

    Array<String> keys = new Array<>();
    Array<Float> weights = new Array<>();

    @Override
    public void loadFrom(RoutineInstance routineInstance, JsonValue nodeData) {
        JsonValue properties = nodeData.get("properties");
        for(int i = 0; i < properties.size; i++) {
            String key = properties.get(i).name;
            float value = properties.get(i).asFloat();

            Port port = new Port();
            port.name = key;
            port.nodeRef = this;
            port.connectionType = ConnectionType.DATA;
            port.dataType = DataType.FLUID;
            port.portType = PortType.INPUT;
            port.valueOverride = value;
            inputs.put(key, port);
        }

        super.loadFrom(routineInstance, nodeData);
    }

    @Override
    public Object queryValue(String targetPortName) {
        weights.clear();
        keys.clear();
        float sum = 0;

        weights.add(0f);
        keys.add("first");

        for(String key: inputs.keys()) {
            Port port = inputs.get(key);
            if(!port.connections.isEmpty()) {
                if(port.portType == PortType.INPUT) {
                    // it is active
                    float val = (float) port.valueOverride;
                    sum += val;
                    weights.add(sum);
                    keys.add(key);
                }
            }
        }

        setSeed();

        float result = random.nextFloat();

        result *= sum;

        if(inputs.size == 0) {
            return null;
        }
        if(inputs.size == 1) {
            return getConnectionOf(inputs.get(keys.get(0)));
        }


        for(int i = 1; i < weights.size; i++) {
            if(result >= weights.get(i-1) && result < weights.get(i)) {
                return getConnectionOf(inputs.get(keys.get(i)));
            }
        }

        return getConnectionOf(inputs.get(keys.get(keys.size-1)));
    }

    private Object getConnectionOf(Port port) {
        if(!port.connections.isEmpty()) {
            Connection connection = port.connections.first();
            RoutineNode targetNode = connection.toPort.nodeRef;
            String targetPortName = connection.toPort.name;

            return targetNode.queryValue(targetPortName);
        }

        return null;
    }
}
