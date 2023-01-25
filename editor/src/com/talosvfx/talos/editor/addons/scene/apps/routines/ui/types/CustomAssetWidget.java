package com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.SelectBoxWidget;
import com.talosvfx.talos.editor.widgets.ui.common.GenericAssetSelectionWidget;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.LabelWithZoom;

import java.util.function.Supplier;

public class CustomAssetWidget extends ATypeWidget<GameAsset<?>> {
    private GenericAssetSelectionWidget<?> assetWidget;
    private final SelectBoxWidget typeSelector;

    private GameAssetType currentType = GameAssetType.SPRITE;

    @Override
    public String getTypeName() {
        return currentType.name();
    }

    @Override
    public void updateFromPropertyWrapper(PropertyWrapper<GameAsset<?>> propertyWrapper) {
        assetWidget.updateWidget(((GameAsset) propertyWrapper.defaultValue));
    }

    @Override
    public void applyValueToWrapper(PropertyWrapper<GameAsset<?>> propertyWrapper) {
        propertyWrapper.defaultValue = assetWidget.getValue();
    }

    public CustomAssetWidget() {

        final Array<String> types = new Array<>();
        GameAssetType[] values = GameAssetType.values();
        for(GameAssetType type: values) {
            types.add(type.name());
        }

        typeSelector = new SelectBoxWidget("type", new Supplier<String>() {
            @Override
            public String get() {
                return currentType.name();
            }
        }, new PropertyWidget.ValueChanged<String>() {
            @Override
            public void report(String value) {
                currentType = GameAssetType.valueOf(value);
                reBuild();

                fireChangedEvent();
            }
        }, new Supplier<Array<String>>() {
            @Override
            public Array<String> get() {
                return types;
            }
        });
        typeSelector.updateWidget(currentType.name());

        reBuild();
    }

    private void reBuild() {
        clearChildren();

        assetWidget = new GenericAssetSelectionWidget<>(currentType);

        Table content = new Table();

        Label label = new LabelWithZoom("asset", SharedResources.skin);
        content.add(label).left().expandX();
        content.add(assetWidget).growX().expandX().right();

        add(typeSelector).padLeft(4).padRight(4).width(220).padTop(9).padBottom(4);
        row();
        add(content).padLeft(4).padRight(4).width(220).padTop(9).padBottom(4);
    }
}
