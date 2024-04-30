package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.BaseAssetRepository;
import com.talosvfx.talos.runtime.routine.serialization.BaseRoutineData;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.routine.RoutineInstance;
import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.routine.TickableNode;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyType;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

public class CallRoutineNode extends RoutineNode implements TickableNode, GameAsset.GameAssetUpdateListener {

    RoutineInstance targetInstance;
    private RoutineInstance.RoutineListenerAdapter listener;

    GameAsset<BaseRoutineData> currentSelectedAsset;

    @Override
    protected void constructNode(XmlReader.Element config) {
        super.constructNode(config);
    }

    @Override
    protected void configureNode(JsonValue properties) {
        // pre construct the custom part before configuring from values?
        // todo: IMPORTANT ASSET IS NOT LOADED YET, we do Runnable hack :/
        GameAsset<BaseRoutineData> asset = null;
        for(JsonValue item: properties) {
            if(item.has("type") && item.getString("type").equals("ROUTINE")) {
                String name = item.name;
                String routineAssetId = item.getString("id");
                //TODO: this needs to be done in a "runtime" friendly way, leaving this task for another time

                //OK WE NEED TO DO SOMETHING HERE
                BaseAssetRepository baseAssetRepository = RuntimeContext.getInstance().getTalosContext(getTalosIdentifier()).getBaseAssetRepository();
                asset = baseAssetRepository.getAssetForIdentifier(routineAssetId, GameAssetType.ROUTINE);

                //TODO HOOK IT UP TOM
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

    public void customConstruction(GameAsset<BaseRoutineData> asset) {
        //todo: ths should be done in a runtime friendly way

        if (currentSelectedAsset != null) {
            currentSelectedAsset.listeners.removeValue(this, true);
        }
        BaseRoutineData resource = asset.getResource();
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
        targetInstance = asset.getResource().createInstance(false, getTalosIdentifier());
        currentSelectedAsset = asset;
        currentSelectedAsset.listeners.add(this);
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

    @Override
    public void onUpdate() {
        if (!currentSelectedAsset.isBroken()) {
            targetInstance = currentSelectedAsset.getResource().createInstance(false, getTalosIdentifier());
        }
    }
}
