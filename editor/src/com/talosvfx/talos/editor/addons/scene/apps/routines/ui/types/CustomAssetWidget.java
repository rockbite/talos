package com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers.PropertyWrapper;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.nodes.widgets.GameAssetWidget;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.ui.common.AssetSelector;

import java.util.function.Supplier;

/**
 * todo: this needs to support all types not just sprites
 */
public class CustomAssetWidget extends ATypeWidget<GameAsset<Texture>> {
    private final AssetSelector<Texture> assetWidget;

    @Override
    public String getTypeName() {
        return "asset";
    }

    @Override
    public void updateFromPropertyWrapper(PropertyWrapper<GameAsset<Texture>> propertyWrapper) {
        assetWidget.updateWidget(propertyWrapper.defaultValue);
    }

    @Override
    public void applyValueToWrapper(PropertyWrapper<GameAsset<Texture>> propertyWrapper) {
        propertyWrapper.defaultValue = assetWidget.getValue();
    }

    public CustomAssetWidget() {
        assetWidget = new AssetSelector<>("sprite", GameAssetType.SPRITE);

        Table content = new Table();

        Label label = new Label("sprite", SharedResources.skin);
        content.add(label).left().expandX();
        content.add(assetWidget).growX().expandX().right();

        add(content).padLeft(4).padRight(4).width(220).padTop(9).padBottom(4);
    }
}
