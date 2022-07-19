package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;

import java.util.function.Supplier;

public class ScriptComponent extends AComponent implements Json.Serializable, GameResourceOwner<String> {


    GameAsset<String> scriptResource;

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        AssetSelectWidget<String> widget = new AssetSelectWidget<String>("Script", GameAssetType.SCRIPT, new Supplier<GameAsset<String>>() {
            @Override
            public GameAsset<String> get() {
                return scriptResource;
            }
        }, new PropertyWidget.ValueChanged<GameAsset<String>>() {
            @Override
            public void report(GameAsset<String> value) {
                setGameAsset(value);
            }
        });

        properties.add(widget);

        return properties;
    }

    @Override
    public String getPropertyBoxTitle () {
        return "Script Component";
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
    public GameAssetType getGameAssetType () {
        return GameAssetType.SCRIPT;
    }

    @Override
    public GameAsset<String> getGameResource () {
        return scriptResource;
    }

    @Override
    public void setGameAsset (GameAsset<String> gameAsset) {
        this.scriptResource = gameAsset;
    }

    @Override
    public void write (Json json) {
        GameResourceOwner.writeGameAsset(json, this);

    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        String gameResourceIdentifier = GameResourceOwner.readGameResourceFromComponent(jsonData);

        loadScriptFromIdentifier(gameResourceIdentifier);

    }

    private void loadScriptFromIdentifier (String gameResourceIdentifier) {
        GameAsset<String> assetForIdentifier = AssetRepository.getInstance().getAssetForIdentifier(gameResourceIdentifier, GameAssetType.SCRIPT);
        setGameAsset(assetForIdentifier);
    }
}
