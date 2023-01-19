package com.talosvfx.talos.runtime.scene.components;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.maps.MapType;
import com.talosvfx.talos.runtime.maps.TalosLayer;
import com.talosvfx.talos.runtime.scene.GameObject;

public class MapComponent extends RendererComponent {

    private Array<TalosLayer> layers = new Array<>();
    private MapType mapType = MapType.ORTHOGRAPHIC_TOPDOWN;
    public transient TalosLayer selectedLayer;

    @Override
    public void minMaxBounds (GameObject parentEntity, BoundingBox rectangle) {
        //todo
    }

    @Override
    public void write (Json json) {
        super.write(json);

        json.writeValue("layers", layers);
        json.writeValue("mapType", mapType);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        layers = json.readValue(Array.class, TalosLayer.class, jsonData.get("layers"));
        mapType = json.readValue(MapType.class, jsonData.get("mapType"));
    }

    public Array<TalosLayer> getLayers () {
        return layers;
    }

    public MapType getMapType () {
        return mapType;
    }

    @Override
    public void reset() {
        super.reset();
        mapType = MapType.ORTHOGRAPHIC_TOPDOWN;
        layers.clear();
    }


}
