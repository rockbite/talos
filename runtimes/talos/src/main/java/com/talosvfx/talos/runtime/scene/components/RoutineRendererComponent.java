package com.talosvfx.talos.runtime.scene.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.assets.GameResourceOwner;
import com.talosvfx.talos.runtime.routine.RoutineInstance;
import com.talosvfx.talos.runtime.routine.serialization.BaseRoutineData;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.ISizableComponent;
import com.talosvfx.talos.runtime.scene.ValueProperty;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


public class RoutineRendererComponent<T extends BaseRoutineData> extends RendererComponent implements Json.Serializable, GameResourceOwner<T>, ISizableComponent {

    GameAsset<T> routineResource;

    GameAsset.GameAssetUpdateListener updateListener;

    public Color color = new Color(Color.WHITE);

    @ValueProperty(prefix = {"W", "H"})
    public Vector2 viewportSize = new Vector2(6, 4);

    @ValueProperty(min = 0, max = 999, step=0.1f)
    public float cacheCoolDown = 0.1f;

    public transient RoutineInstance routineInstance;

    public Array<PropertyWrapper<?>> propertyWrappers = new Array<>();

    @Setter@Getter
    private boolean requiresWrite;

    public RoutineRendererComponent() {
        updateListener = new GameAsset.GameAssetUpdateListener() {
            @Override
            public void onUpdate() {
                RoutineInstance instance = RoutineRendererComponent.this.routineResource.getResource().createInstance(true);
                routineInstance = instance;
                updatePropertyWrappers(true);
            }
        };
    }

    @Override
    public void write(Json json) {
        super.write(json);
        GameResourceOwner.writeGameAsset(json, this);
        json.writeValue("size", viewportSize, Vector2.class);
        json.writeValue("cache", cacheCoolDown);
        json.writeValue("properties", propertyWrappers);
        json.writeValue("color", color);

        requiresWrite = false;
    }

    private void loadRoutineFromIdentifier (String gameResourceIdentifier) {
        GameAsset<T> assetForIdentifier = RuntimeContext.getInstance().AssetRepository.getAssetForIdentifier(gameResourceIdentifier, GameAssetType.ROUTINE);
        setGameAsset(assetForIdentifier);
    }

    private void loadRoutineFromUniqueIdentifier (UUID gameResourceUUID) {
        GameAsset<T> assetForUniqueIdentifier = RuntimeContext.getInstance().AssetRepository.getAssetForUniqueIdentifier(gameResourceUUID, GameAssetType.ROUTINE);
        setGameAsset(assetForUniqueIdentifier);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);

        propertyWrappers.clear();
        JsonValue propertiesJson = jsonData.get("properties");
        if (propertiesJson != null) {
            for (JsonValue propertyJson : propertiesJson) {
                propertyWrappers.add(json.readValue(PropertyWrapper.class, propertyJson));
            }
        }

        UUID gameResourceUUID = GameResourceOwner.readGameResourceUUIDFromComponent(jsonData);
        if (gameResourceUUID == null) {
            String gameResourceIdentifier = GameResourceOwner.readGameResourceFromComponent(jsonData);
            loadRoutineFromIdentifier(gameResourceIdentifier);
        } else {
            loadRoutineFromUniqueIdentifier(gameResourceUUID);
        }


        viewportSize = json.readValue(Vector2.class, jsonData.get("size"));
        if (viewportSize == null) viewportSize = new Vector2(6, 4);

        cacheCoolDown = jsonData.getFloat("cache", 0.1f);

        color = json.readValue(Color.class, jsonData.get("color"));
        if(color == null) color = new Color(Color.WHITE);
    }


    @Override
    public void reset() {
        super.reset();
        color.set(Color.WHITE);
    }

    public void updatePropertyWrappers (boolean tryToMerge) {
        Array<PropertyWrapper<?>> copyWrappers = new Array<>();
        copyWrappers.addAll(propertyWrappers);

        propertyWrappers.clear();
        if (routineInstance != null) {
            boolean needsToUpdate = needsToUpdate(copyWrappers, routineInstance.getParentPropertyWrappers());
            if (needsToUpdate) {
                requiresWrite = true;
            }

            for (PropertyWrapper<?> propertyWrapper : routineInstance.getParentPropertyWrappers()) {
                if (tryToMerge) {
                    boolean foundCopy = false;
                    for (PropertyWrapper<?> copyWrapper : copyWrappers) {
                        if (copyWrapper.index == propertyWrapper.index) {
                            foundCopy = true;
                            if (copyWrapper.isValueOverridden) {
                                propertyWrappers.add(copyWrapper.clone());
                            } else {
                                final PropertyWrapper<?> clone = propertyWrapper.clone();
                                clone.setDefault();
                                propertyWrappers.add(clone);
                            }
                            break;
                        }
                    }

                    if (!foundCopy) {
                        final PropertyWrapper<?> clone = propertyWrapper.clone();
                        clone.setDefault();
                        propertyWrappers.add(clone);
                    }
                } else {
                    final PropertyWrapper<?> clone = propertyWrapper.clone();
                    clone.setDefault();
                    propertyWrappers.add(clone);
                }
            }
        }
    }

    private boolean needsToUpdate (Array<PropertyWrapper<?>> existingWrappers, Array<PropertyWrapper<?>> truthWrappers) {
        for (int i = 0; i < truthWrappers.size; i++) {
            PropertyWrapper<?> truthWrapper = truthWrappers.get(i);
            int indexToFind = truthWrapper.index;

            PropertyWrapper<?> wrapperForIndex = getWrapperForIndex(indexToFind, existingWrappers);
            if (wrapperForIndex == null) {
                return true;
            }
            if (wrapperForIndex.getType() != truthWrapper.getType()) {
                return true;
            }
            if (!(wrapperForIndex.propertyName.equals(truthWrapper.propertyName))) {
                return true;
            }
            if (!(wrapperForIndex.defaultValue.equals(truthWrapper.defaultValue))) {
                return true;
            }
        }
        return false;
    }

    private PropertyWrapper<?> getWrapperForIndex (int index, Array<PropertyWrapper<?>> wrappers) {
        for (int i = 0; i < wrappers.size; i++) {
            PropertyWrapper<?> propertyWrapper = wrappers.get(i);
            if (propertyWrapper.index == index) {
                return propertyWrapper;
            }
        }
        return null;
    }

    @Override
    public void minMaxBounds(GameObject parentEntity, BoundingBox rectangle) {

    }

    @Override
    public GameAssetType getGameAssetType() {
        return GameAssetType.ROUTINE;
    }

    @Override
    public GameAsset<T> getGameResource() {
        return routineResource;
    }

    @Override
    public void setGameAsset(GameAsset<T> gameAsset) {
        if (routineResource != null) {
            routineResource.listeners.removeValue(updateListener, true);
        }
        this.routineResource = gameAsset;
        gameAsset.listeners.add(updateListener);

        if (!routineResource.isBroken()) {
            routineInstance = routineResource.getResource().createInstance(true);
            updatePropertyWrappers(true);
        }
    }



    @Override
    public boolean allowsMultipleOfTypeOnGameObject () {
        return false;
    }

    @Override
    public float getWidth() {
        return viewportSize.x;
    }

    @Override
    public float getHeight() {
        return viewportSize.y;
    }

    @Override
    public void setWidth(float width) {
        viewportSize.x = width;
    }

    @Override
    public void setHeight(float height) {
        viewportSize.y = height;
    }
}
