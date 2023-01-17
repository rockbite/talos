package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineInstance;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers.PropertyWrapper;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.data.RoutineStageData;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ValueProperty;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Supplier;

public class RoutineRendererComponent extends RendererComponent implements Json.Serializable, GameResourceOwner<RoutineStageData>, ISizableComponent {

    GameAsset<RoutineStageData> routineResource;

    GameAsset.GameAssetUpdateListener updateListener;

    Array<PropertyWidget> properties = new Array<>();

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

        requiresWrite = false;
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        String gameResourceIdentifier = GameResourceOwner.readGameResourceFromComponent(jsonData);

        propertyWrappers.clear();
        JsonValue propertiesJson = jsonData.get("properties");
        if (propertiesJson != null) {
            for (JsonValue propertyJson : propertiesJson) {
                propertyWrappers.add(json.readValue(PropertyWrapper.class, propertyJson));
            }
        }

        GameAsset<RoutineStageData> assetForIdentifier = AssetRepository.getInstance().getAssetForIdentifier(gameResourceIdentifier, GameAssetType.ROUTINE);
        setGameAsset(assetForIdentifier);

        viewportSize = json.readValue(Vector2.class, jsonData.get("size"));
        if (viewportSize == null) viewportSize = new Vector2(6, 4);

        cacheCoolDown = jsonData.getFloat("cache", 0.1f);
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
                                PropertyWrapper<?> cloneWrapper = propertyWrapper.clone();
                                cloneWrapper.setDefault();
                                propertyWrappers.add(cloneWrapper);
                            }
                            break;
                        }
                    }

                    if (!foundCopy) {
                        propertyWrappers.add(propertyWrapper.clone());
                    }
                } else {
                    propertyWrappers.add(propertyWrapper.clone());
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
    public Array<PropertyWidget> getListOfProperties() {
        properties.clear();
        AssetSelectWidget<RoutineStageData> widget = new AssetSelectWidget<RoutineStageData>("Routine", GameAssetType.ROUTINE, new Supplier<GameAsset<RoutineStageData>>() {
            @Override
            public GameAsset<RoutineStageData> get() {
                return routineResource;
            }
        }, new PropertyWidget.ValueChanged<GameAsset<RoutineStageData>>() {
            @Override
            public void report(GameAsset<RoutineStageData> value) {
                setGameAsset(value);
                final GameObject gameObject = RoutineRendererComponent.this.getGameObject();
                SceneUtils.componentUpdated(gameObject.getGameObjectContainerRoot(), gameObject, RoutineRendererComponent.this);
            }
        });

        properties.add(widget);

        PropertyWidget sizeWidget = WidgetFactory.generate(this, "viewportSize", "Viewport");
        properties.add(sizeWidget);

        PropertyWidget cacheWidget = WidgetFactory.generate(this, "cacheCoolDown", "Cache");
        properties.add(cacheWidget);

        Array<PropertyWidget> superList = super.getListOfProperties();
        properties.addAll(superList);

        for (PropertyWrapper<?> propertyWrapper : propertyWrappers) {
            PropertyWidget generate = WidgetFactory.generateForPropertyWrapper(propertyWrapper);
            generate.setInjectedChangeListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    propertyWrapper.isValueOverridden = true;
                    RoutineRendererComponent.this.routineInstance.setDirty();
                }
            });
            generate.setParent(this);
            properties.add(generate);
        }

        return properties;
    }

    @Override
    public String getPropertyBoxTitle() {
        return "Routine Renderer";
    }

    @Override
    public int getPriority() {
        return 4;
    }

    @Override
    public GameAssetType getGameAssetType() {
        return GameAssetType.ROUTINE;
    }

    @Override
    public GameAsset<RoutineStageData> getGameResource() {
        return routineResource;
    }

    @Override
    public void setGameAsset(GameAsset<RoutineStageData> gameAsset) {
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
