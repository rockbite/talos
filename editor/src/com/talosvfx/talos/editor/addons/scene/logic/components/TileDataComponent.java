package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.maps.GridPosition;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;

import java.util.function.Supplier;

public class TileDataComponent extends AComponent implements Json.Serializable {

    /**
     * Used in depth sorting for top down ortho maps
     */
    private float fakeZ = 0;

    /**
     * The visual offset relative to the bottom left most parent tile
     */
    private Vector2 visualOffset = new Vector2();

    /**
     * Set of 2d tiles that represent the parent tiles. Should be at least 1 tile always, but can be any with no shape restrictions
     * This is 'footprint' of the game asset in world space. Ignoring sprite/skeleton bounds, but the tiles this object takes up in world space
     */
    private ObjectSet<GridPosition> parentTiles = new ObjectSet<>();

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        properties.add(WidgetFactory.generate(this, "fakeZ", "FakeZ"));
        properties.add(WidgetFactory.generate(this, "visualOffset", "VisualOffset"));

        return properties;
    }

    @Override
    public String getPropertyBoxTitle () {
        return "TileData Component";
    }

    @Override
    public int getPriority () {
        return 4;
    }

    @Override
    public Class<? extends IPropertyProvider> getType () {
        return getClass();
    }

    @Override
    public void write (Json json) {
        json.writeValue("fakeZ", fakeZ);
        json.writeValue("visualOffsetX", visualOffset.x);
        json.writeValue("visualOffsetY", visualOffset.y);

        json.writeArrayStart("parentTiles");
        for (GridPosition parentTile : parentTiles) {
            json.writeValue(parentTile);
        }
        json.writeArrayEnd();

    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        fakeZ = jsonData.getFloat("fakeZ", fakeZ);
        visualOffset.x = jsonData.getFloat("visualOffsetX", visualOffset.x);
        visualOffset.y = jsonData.getFloat("visualOffsetY", visualOffset.y);
        if (jsonData.has("parentTiles")) {
            JsonValue parentTiles = jsonData.get("parentTiles");
            for (JsonValue tile : parentTiles) {
                GridPosition gridPosition = json.readValue(GridPosition.class, tile);
                this.parentTiles.add(gridPosition);
            }
        }
    }

    public float getFakeZ () {
        return fakeZ;
    }

    public void setFakeZ(float fakeZ) {
        this.fakeZ = fakeZ;
    }
}
