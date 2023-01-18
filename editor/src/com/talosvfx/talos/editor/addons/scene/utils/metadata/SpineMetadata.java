package com.talosvfx.talos.editor.addons.scene.utils.metadata;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.assets.AMetadata;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;

import java.util.function.Supplier;

public class SpineMetadata extends AMetadata {

    public float pixelsPerUnit = DefaultConstants.PIXELS_PER_UNIT;

    public String atlasPath;

    public SpineMetadata() {
        super();
    }

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> propertyWidgets = new Array<>();

        PropertyWidget pixelPerUnit = WidgetFactory.generate(this, "pixelsPerUnit", "pxToWorld");
        pixelPerUnit.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                //Reload the game asset with new import scale
                AssetRepository.getInstance().reloadGameAssetForRawFile(link);
            }
        });
        propertyWidgets.add(pixelPerUnit);

        AssetSelectWidget atlasWidget = new AssetSelectWidget("Atlas", GameAssetType.ATLAS, new Supplier<String>() {
            @Override
            public String get() {
                return atlasPath;
            }
        }, new PropertyWidget.ValueChanged<String>() {
            @Override
            public void report(String value) {
                atlasPath = value;
            }
        });
        propertyWidgets.add(atlasWidget);

        return propertyWidgets;
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        pixelsPerUnit = jsonData.getFloat("pixelsPerUnit", pixelsPerUnit);
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("pixelsPerUnit", pixelsPerUnit);
    }

    @Override
    public String getPropertyBoxTitle () {
        return "Skeleton Data";
    }
}
