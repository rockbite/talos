package com.talosvfx.talos.editor.nodes.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.SelectBoxWidget;
import com.talosvfx.talos.editor.widgets.ui.common.GenericAssetSelectionWidget;
import lombok.Getter;

import java.util.function.Supplier;

public class GameAssetWidget<T> extends AbstractWidget<GameAsset<T>> {

    private final SelectBoxWidget typeSelector;
    private Cell<SelectBoxWidget> typeSelectorCell;
    private GameAsset<T> gameAsset;
    private GameAssetType type;
    @Getter
    private GenericAssetSelectionWidget<T> widget;

    private Table bottomContainer;

    public GameAssetWidget() {

        final Array<String> types = new Array<>();
        GameAssetType[] values = GameAssetType.values();
        for(GameAssetType type: values) {
            types.add(type.name());
        }

        typeSelector = new SelectBoxWidget("type", new Supplier<String>() {
            @Override
            public String get() {
                return type.name();
            }
        }, new PropertyWidget.ValueChanged<String>() {
            @Override
            public void report(String value) {
                type = GameAssetType.valueOf(value);
                build(type.toString());
                fireChangedEvent();
            }
        }, new Supplier<Array<String>>() {
            @Override
            public Array<String> get() {
                return types;
            }
        });
        type=GameAssetType.SPRITE;
        typeSelector.updateWidget(type.name());

        typeSelectorCell = content.add(typeSelector).growX().padBottom(4).padRight(1);
        content.row();

        bottomContainer = new Table();
        content.add(bottomContainer).growX();
    }

    public void build(String typeString) {
        bottomContainer.clear();

        type = GameAssetType.valueOf(typeString);

        widget = new GenericAssetSelectionWidget<>(type);
        widget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                gameAsset = widget.getValue();
                fireChangedEvent();
            }
        });

        bottomContainer.add(widget).growX();
    }

    @Override
    public void loadFromXML(XmlReader.Element element) {
        String typeString = element.getAttribute("type", "");
        if(typeString.equals("")) {
            // widget is generic
            build("SPRITE");
        } else {
            build(typeString);
            typeSelectorCell.setActor(null).pad(0);
        }


    }

    @Override
    public GameAsset<T> getValue() {
        return gameAsset;
    }

    @Override
    public void read(Json json, JsonValue jsonValue) {
        try {
            GameAssetType type = json.readValue("type", GameAssetType.class, jsonValue);
            String uuid = readGameAssetUniqueIdentifier(jsonValue);
            if (uuid.equals("broken")) {
                String id = readGameAssetIdentifier(jsonValue);
                gameAsset = AssetRepository.getInstance().getAssetForIdentifier(id, type);
            } else {
                gameAsset = AssetRepository.getInstance().getAssetForUniqueIdentifier(uuid, type);
            }

            this.type = type;
            typeSelector.updateWidget(type.name());

            widget.updateWidget(gameAsset);
        } catch (Exception e) {}
    }

    static String readGameAssetIdentifier (JsonValue jsonValue) {
        return jsonValue.getString("id", "broken");
    }

    static String readGameAssetUniqueIdentifier (JsonValue jsonValue) {
        return jsonValue.getString("uuid", "broken");
    }

    @Override
    public void write(Json json, String name) {
        json.writeObjectStart(name);
        if(gameAsset != null) {
            json.writeValue("type", type);
            json.writeValue("id", gameAsset.nameIdentifier);
            json.writeValue("uuid", gameAsset.getRootRawAsset().metaData.uuid.toString());
        }
        json.writeObjectEnd();
    }
}
