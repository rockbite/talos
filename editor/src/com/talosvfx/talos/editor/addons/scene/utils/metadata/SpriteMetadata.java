package com.talosvfx.talos.editor.addons.scene.utils.metadata;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;

public class SpriteMetadata extends AMetadata {

    public int[] borderData;

    public float pixelsPerUnit = 100;

    public Texture.TextureFilter filterMode = Texture.TextureFilter.Nearest;

    public SpriteMetadata() {

    }

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> propertyWidgets = new Array<>();

        propertyWidgets.add(WidgetFactory.generate(this, "pixelsPerUnit", "pxToWorld"));
        propertyWidgets.add(WidgetFactory.generate(this, "filterMode", "Filter"));

        return propertyWidgets;
    }

    @Override
    public String getPropertyBoxTitle () {
        return "Sprite";
    }
}
