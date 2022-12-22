package com.talosvfx.talos.editor.nodes.widgets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;

import java.util.function.Supplier;

public class GameAssetWidget extends AbstractWidget<GameAsset> {

    private AssetSelectWidget widget;
    private GameAsset gameAsset;
    private GameAssetType type;


    @Override
    public void loadFromXML(XmlReader.Element element) {

        String typeString = element.getAttribute("type");
        type = GameAssetType.valueOf(typeString);

        widget = new AssetSelectWidget<>(element.getText(), type, new Supplier<GameAsset<Texture>>() {
            @Override
            public GameAsset<Texture> get() {
                return gameAsset;
            }
        }, new PropertyWidget.ValueChanged<GameAsset<Texture>>() {
            @Override
            public void report(GameAsset<Texture> value) {
                gameAsset = value;
                fireChangedEvent();
            }
        });

        add(widget).growX();
    }

    @Override
    public GameAsset getValue() {
        return gameAsset;
    }

    @Override
    public void read(Json json, JsonValue jsonValue) {
        String identifier = jsonValue.asString();
        gameAsset = AssetRepository.getInstance().getAssetForIdentifier(identifier, type);

        widget.updateWidget(gameAsset);
    }

    @Override
    public void write(Json json, String name) {
        if(gameAsset == null) return;
        json.writeValue(name, gameAsset.nameIdentifier);
    }
}
