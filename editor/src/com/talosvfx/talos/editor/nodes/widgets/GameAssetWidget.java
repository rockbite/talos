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
import com.talosvfx.talos.editor.widgets.ui.common.AssetSelector;
import lombok.Getter;

import java.util.function.Supplier;

public class GameAssetWidget<T> extends AbstractWidget<GameAsset<T>> {

    private final SelectBoxWidget typeSelector;
    private Cell<SelectBoxWidget> typeSelectorCell;
    private GameAsset<T> gameAsset;
    private GameAssetType type;

    @Getter
    private AssetSelector<T> widget;

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
                build(type.toString(), "");
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

    public void build(String typeString, String text) {
        bottomContainer.clear();

        type = GameAssetType.valueOf(typeString);

        widget = new AssetSelector<>(text, type);
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
            build("SPRITE", "");
        } else {
            build(typeString, "");
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
            String identifier = jsonValue.getString("id");

            gameAsset = AssetRepository.getInstance().getAssetForIdentifier(identifier, type);
            this.type = type;
            typeSelector.updateWidget(type.name());

            widget.updateWidget(gameAsset);
        } catch (Exception e) {}
    }

    @Override
    public void write(Json json, String name) {
        json.writeObjectStart(name);
        if(gameAsset != null) {
            json.writeValue("type", type);
            json.writeValue("id", gameAsset.nameIdentifier);
        }
        json.writeObjectEnd();
    }
}
