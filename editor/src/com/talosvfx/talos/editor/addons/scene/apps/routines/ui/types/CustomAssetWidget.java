package com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types;

import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;

import java.util.function.Supplier;

/**
 * todo: this needs to support all types not hust sprites
 */
public class CustomAssetWidget extends ATypeWidget {
    private final AssetSelectWidget assetWidget;

    @Override
    public String getTypeName() {
        return "asset";
    }

    public CustomAssetWidget() {
        assetWidget = new AssetSelectWidget("sprite", GameAssetType.SPRITE, new Supplier<GameAsset>() {
            @Override
            public GameAsset get() {
                return null;
            }
        }, new PropertyWidget.ValueChanged<GameAsset>() {
            @Override
            public void report(GameAsset value) {

            }
        });

        add(assetWidget).padLeft(4).padRight(4).width(220).padTop(9);
    }
}
