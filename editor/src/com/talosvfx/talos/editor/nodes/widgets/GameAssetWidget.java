package com.talosvfx.talos.editor.nodes.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.widgets.ui.common.AssetSelector;

import java.util.function.Supplier;

public class GameAssetWidget extends AbstractWidget<GameAsset> {

    private GameAsset gameAsset;
    private GameAssetType type;
    private AssetSelector<Object> widget;


    @Override
    public void loadFromXML(XmlReader.Element element) {

        String typeString = element.getAttribute("type");

        type = GameAssetType.valueOf(typeString);
        String text = element.getText();

        widget = new AssetSelector<>(element.getText(), type);
        widget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                gameAsset = widget.getValue();
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
