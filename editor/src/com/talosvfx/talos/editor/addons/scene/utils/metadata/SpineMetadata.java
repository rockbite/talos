package com.talosvfx.talos.editor.addons.scene.utils.metadata;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ValueProperty;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;

import java.util.function.Supplier;

public class SpineMetadata extends AMetadata {

    @ValueProperty(min = 0.001f, max = 2, step = 0.001f)
    public float scale = 0.01f;

    public String atlasPath;

    public SpineMetadata() {
        super();
    }

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> propertyWidgets = new Array<>();

        propertyWidgets.add(WidgetFactory.generate(this, "scale", "Scale"));

        AssetSelectWidget atlasWidget = new AssetSelectWidget("Atlas", "atlas", new Supplier<String>() {
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
