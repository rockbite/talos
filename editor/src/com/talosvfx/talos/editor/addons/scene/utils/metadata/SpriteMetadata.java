package com.talosvfx.talos.editor.addons.scene.utils.metadata;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;

public class SpriteMetadata extends AMetadata {

    public int[] borderData;

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        return null;
    }

    @Override
    public String getPropertyBoxTitle () {
        return "Sprite";
    }
}
