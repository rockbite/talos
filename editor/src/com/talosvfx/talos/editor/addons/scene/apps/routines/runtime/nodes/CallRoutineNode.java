package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlReader;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineInstance;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.TickableNode;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers.PropertyType;
import com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers.PropertyWrapper;
import com.talosvfx.talos.editor.data.RoutineStageData;
import com.talosvfx.talos.editor.project2.SharedResources;

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
                asset = AssetRepository.getInstance().getAssetForIdentifier(routineAssetId, GameAssetType.ROUTINE);
                if(asset != null) {
                    customConstruction(asset);
                }
            }
        }

        super.configureNode(properties);

        configured = false;
    }

    public void customConstruction(GameAsset<RoutineStageData> asset) {
        //todo: ths should be done in a runtime friendly way
        RoutineStageData resource = asset.getResource();
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
            listener.terminate();
            targetInstance.removeListener();

            targetInstance = null;
        }
    }
}
