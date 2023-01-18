package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.routine.RoutineInstance;
import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.routine.TickableNode;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyType;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

public class CallRoutineNode extends RoutineNode implements TickableNode {


    RoutineInstance targetInstance;
    private RoutineInstance.RoutineListenerAdapter listener;

    @Override
    protected void constructNode(XmlReader.Element config) {
        super.constructNode(config);
    }

    @Override
    protected void configureNode(JsonValue properties) {
        // pre construct the custom part before configuring from values?
        // todo: IMPORTANT ASSET IS NOT LOADED YET, we do Runnable hack :/
        GameAsset<RoutineStageData> asset = null;
        for(JsonValue item: properties) {
            if(item.has("type") && item.getString("type").equals("ROUTINE")) {
                String name = item.name;
                String routineAssetId = item.getString("id");
                //TODO: this needs to be done in a "runtime" friendly way, leaving this task for another time
                asset = RuntimeContext.getInstance().AssetRepository.getAssetForIdentifier(routineAssetId, GameAssetType.ROUTINE);
                if(asset != null) {
                    customConstruction(asset);
                }
            }
        }

        super.configureNode(properties);

        if(!configured) {
            configured = false;
        }
    }

    public void customConstruction(GameAsset<RoutineStageData> asset) {
        //todo: ths should be done in a runtime friendly way
        RoutineStageData resource = asset.getResource();
        if(resource == null) {
            configured = false;
            return;
        }
        Array<PropertyWrapper<?>> propertyWrappers = resource.getPropertyWrappers();

        for (PropertyWrapper<?> propertyWrapper : propertyWrappers) {
            PropertyType type = propertyWrapper.getType();

            Port port = new Port();
            port.name = propertyWrapper.propertyName;
            port.nodeRef = this;
            port.connectionType = ConnectionType.DATA;
            port.dataType = makeDataType(type);
            port.portType = PortType.INPUT;
            inputs.put( port.name, port);
        }

        Port port = new Port();
        port.name = "executorName";
        port.nodeRef = this;
        port.connectionType = ConnectionType.DATA;
        port.dataType = DataType.STRING;
        port.portType = PortType.INPUT;
        inputs.put( port.name, port);

        // now create that routine
        targetInstance = asset.getResource().createInstance(false);
    }

    private DataType makeDataType(PropertyType type) {
        if(type == PropertyType.FLOAT) return DataType.NUMBER;
        if(type == PropertyType.ASSET) return DataType.ASSET;
        if(type == PropertyType.BOOLEAN) return DataType.BOOLEAN;
        if(type == PropertyType.COLOR) return DataType.COLOR;
        if(type == PropertyType.VECTOR2) return DataType.VECTOR2;

        return DataType.NUMBER;
    }

    @Override
    public void receiveSignal(String portName) {
        if(targetInstance != null) {
            //todo: setup custom var values (fetch them first)
            Array<PropertyWrapper<?>> wrappers = targetInstance.getParentPropertyWrappers();
            for (PropertyWrapper<?> wrapper : wrappers) {
                String propertyName = wrapper.propertyName;
                Object val = fetchValue(propertyName);
                if(val != null) {
                    wrapper.setValueUnsafe(val);
                    targetInstance.getProperties().put(wrapper.propertyName, wrapper);
                }
            }

            targetInstance.setContainer(routineInstanceRef.getContainer());

            listener = new RoutineInstance.RoutineListenerAdapter() {
                @Override
                public void onComplete() {
                    routineInstanceRef.setSignalPayload(targetInstance.getSignalPayload());
                    sendSignal("onComplete");
                }
            };
            targetInstance.setListener(listener);

            String executorName = fetchStringValue("executorName");
            if(fetchBooleanValue("payloadOverride")) {
                targetInstance.setSignalPayload(routineInstanceRef.getSignalPayload());
            } else {
                targetInstance.setSignalPayload(null);
            }
            RoutineExecutorNode node = (RoutineExecutorNode) targetInstance.getCustomLookup().get(executorName);
            node.receiveSignal("startSignal");
        }
    }

    @Override
    public void tick(float delta) {
        if(targetInstance != null) {
            targetInstance.tick(delta);
        }
    }

    @Override
    public void reset() {
        super.reset();
        if(targetInstance != null) {
            targetInstance.reset();
            if(listener != null) {
                listener.terminate();
            }
            targetInstance.removeListener();

            targetInstance.reset();
        }
    }
}
