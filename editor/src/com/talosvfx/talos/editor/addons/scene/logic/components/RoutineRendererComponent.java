package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.RoutineConfigMap;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.RoutineInstance;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.utils.scriptProperties.PropertyWrapper;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ValueProperty;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;

import java.util.function.Supplier;

public class RoutineRendererComponent extends RendererComponent implements Json.Serializable, GameResourceOwner<String> {

    GameAsset<String> routineResource;

    @ValueProperty(prefix = {"W", "H"})
    public Vector2 viewportSize = new Vector2(6, 4);


    public transient RoutineInstance routineInstance;

    public Array<PropertyWrapper<?>> propertyWrappers = new Array<>();

    public RoutineRendererComponent() {

    }

    @Override
    public void write(Json json) {
        super.write(json);
        GameResourceOwner.writeGameAsset(json, this);
        json.writeValue("size", viewportSize, Vector2.class);
        json.writeValue("properties", propertyWrappers);
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

        GameAsset<String> assetForIdentifier = AssetRepository.getInstance().getAssetForIdentifier(gameResourceIdentifier, GameAssetType.ROUTINE);
        setGameAsset(assetForIdentifier);

        viewportSize = json.readValue(Vector2.class, jsonData.get("size"));
        if (viewportSize == null) viewportSize = new Vector2(6, 4);
    }

    public void updatePropertyWrappers (boolean tryToMerge) {
        Array<PropertyWrapper<?>> copyWrappers = new Array<>();
        copyWrappers.addAll(propertyWrappers);

        propertyWrappers.clear();
        if (routineInstance != null) {
            for (PropertyWrapper<?> propertyWrapper : routineInstance.getPropertyWrappers()) {
                propertyWrappers.add(propertyWrapper);
            }
        }

        if (tryToMerge) {
            for (PropertyWrapper copyWrapper : copyWrappers) {
                for (PropertyWrapper propertyWrapper : propertyWrappers) {
                    if (copyWrapper.index == propertyWrapper.index && copyWrapper.getClass().equals(propertyWrapper.getClass())) {
                        propertyWrapper.setValue(copyWrapper.getValue());
                        break;
                    }
                }
            }

            for (PropertyWrapper<?> propertyWrapper : propertyWrappers) {
                if (propertyWrapper.getValue() == null) {
                    propertyWrapper.setDefault();
                }
            }

        } else {
            for (PropertyWrapper<?> propertyWrapper : propertyWrappers) {
                propertyWrapper.setDefault();
            }
        }
    }

    @Override
    public void minMaxBounds(GameObject parentEntity, BoundingBox rectangle) {

    }


    @Override
    public Array<PropertyWidget> getListOfProperties() {
        Array<PropertyWidget> properties = new Array<>();

        AssetSelectWidget<String> widget = new AssetSelectWidget<String>("Routine", GameAssetType.ROUTINE, new Supplier<GameAsset<String>>() {
            @Override
            public GameAsset<String> get () {
                return routineResource;
            }
        }, new PropertyWidget.ValueChanged<GameAsset<String>>() {
            @Override
            public void report (GameAsset<String> value) {
                setGameAsset(value);
            }
        });

        properties.add(widget);

        PropertyWidget sizeWidget = WidgetFactory.generate(this, "viewportSize", "Viewport");
        properties.add(sizeWidget);

        Array<PropertyWidget> superList = super.getListOfProperties();
        properties.addAll(superList);

        for (PropertyWrapper<?> propertyWrapper : propertyWrappers) {
            PropertyWidget generate = WidgetFactory.generateForPropertyWrapper(propertyWrapper);
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
    public GameAsset<String> getGameResource() {
        return routineResource;
    }

    @Override
    public void setGameAsset(GameAsset<String> gameAsset) {
        this.routineResource = gameAsset;

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run () {
                //Notifications.fireEvent(Notifications.obtainEvent(ComponentUpdated.class).set(RoutineRendererComponent.this, false));
            }
        });

        routineInstance = new RoutineInstance();
        // this needs changing
        RoutineConfigMap routineConfigMap = new RoutineConfigMap();
        routineConfigMap.loadFrom(Gdx.files.internal("addons/scene/tween-nodes.xml")); //todo: totally not okay
        routineInstance.loadFrom(gameAsset.getRootRawAsset().metaData.uuid, gameAsset.getResource(), routineConfigMap);
        updatePropertyWrappers(true);
    }



    @Override
    public boolean allowsMultipleOfTypeOnGameObject () {
        return false;
    }
}
