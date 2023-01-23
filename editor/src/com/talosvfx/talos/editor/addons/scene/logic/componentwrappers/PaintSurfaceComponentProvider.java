package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.scene.components.PaintSurfaceComponent;

import java.util.function.Supplier;

public class PaintSurfaceComponentProvider extends AComponentProvider<PaintSurfaceComponent> {

	public PaintSurfaceComponentProvider (PaintSurfaceComponent component) {
		super(component);
	}

	@Override
	public Array<PropertyWidget> getListOfProperties () {

		Array<PropertyWidget> properties = new Array<>();

		AssetSelectWidget<Texture> textureWidget = new AssetSelectWidget<>("Texture", GameAssetType.SPRITE, new Supplier<GameAsset<Texture>>() {
			@Override
			public GameAsset<Texture> get () {
				return component.gameAsset;
			}
		}, new PropertyWidget.ValueChanged<GameAsset<Texture>>() {
			@Override
			public void report (GameAsset<Texture> value) {
				component.setGameAsset(value);
			}
		});

		PropertyWidget sizeWidget = WidgetFactory.generate(component, "size", "Size");

		PropertyWidget overlayWidget = WidgetFactory.generate(component, "overlay", "Overlay");

		PropertyWidget redChannelWidget = WidgetFactory.generate(component, "redChannel", "Red Channel");
		PropertyWidget greenChannelWidget = WidgetFactory.generate(component, "greenChannel", "Green Channel");
		PropertyWidget blueChannelWidget = WidgetFactory.generate(component, "blueChannel", "Blue Channel");
		PropertyWidget alphaChannelWidget = WidgetFactory.generate(component, "alphaChannel", "Alpha Channel");

		properties.add(textureWidget);
		properties.add(sizeWidget);
		properties.add(overlayWidget);

		properties.add(redChannelWidget);
		properties.add(greenChannelWidget);
		properties.add(blueChannelWidget);
		properties.add(alphaChannelWidget);

		return properties;
	}

	@Override
	public String getPropertyBoxTitle () {
		return "Paint Surface";
	}

	@Override
	public int getPriority () {
		return 4;
	}
}
