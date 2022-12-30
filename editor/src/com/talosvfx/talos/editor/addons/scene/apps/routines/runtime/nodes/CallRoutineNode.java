package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers.PropertyType;
import com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers.PropertyWrapper;
import com.talosvfx.talos.editor.data.RoutineStageData;

public class CallRoutineNode extends RoutineNode {



    @Override
    protected void constructNode(XmlReader.Element config) {
        super.constructNode(config);
    }

    @Override
    protected void configureNode(JsonValue properties) {
        // pre construct the custom part before configuring from values?
        for(JsonValue item: properties) {
            if(item.has("type") && item.getString("type").equals("ROUTINE")) {
                String name = item.name;
                String routineAssetId = item.getString("id");
                //TODO: this needs to be done in a "runtime" friendly way, leaving this task for another time
                GameAsset<RoutineStageData> asset = AssetRepository.getInstance().getAssetForIdentifier(routineAssetId, GameAssetType.ROUTINE);

                customConstruction(asset);
            }
        }

        super.configureNode(properties);
    }

    private void customConstruction(GameAsset<RoutineStageData> asset) {
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
    }

    private DataType makeDataType(PropertyType type) {
        if(type == PropertyType.FLOAT) return DataType.NUMBER;
        if(type == PropertyType.ASSET) return DataType.ASSET;
        if(type == PropertyType.BOOLEAN) return DataType.BOOLEAN;
        if(type == PropertyType.COLOR) return DataType.COLOR;
        if(type == PropertyType.VECTOR2) return DataType.VECTOR2;

        return DataType.NUMBER;
    }
}
