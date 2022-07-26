package com.talosvfx.talos.editor.addons.scene.utils.metadata;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ValueProperty;
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

        propertyWidgets.add(WidgetFactory.generate(this, "pixelsPerUnit", "pxToWorld"));

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
    public String getPropertyBoxTitle () {
        return "Skeleton Data";
    }
}
