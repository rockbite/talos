package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.editor.addons.scene.events.PaintSurfaceResize;
import com.talosvfx.talos.editor.addons.scene.widgets.property.PropertyPanelAssetSelectionWidget;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectContainer;
import com.talosvfx.talos.runtime.scene.components.PaintSurfaceComponent;

import java.util.function.Supplier;

public class PaintSurfaceComponentProvider extends AComponentProvider<PaintSurfaceComponent> {

	public PaintSurfaceComponentProvider (PaintSurfaceComponent component) {
		super(component);
	}

	@Override
	public Array<PropertyWidget> getListOfProperties () {

		Array<PropertyWidget> properties = new Array<>();

		PropertyWidget textureWidget = WidgetFactory.generateForGameAsset(component, "gameAsset", null, "Texture", GameAssetType.SPRITE);

		PropertyWidget sizeWidget = WidgetFactory.generate(component, "size", "Size");
		sizeWidget.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (!sizeWidget.isFastChange()) {
					Notifications.fireEvent(Notifications.obtainEvent(PaintSurfaceResize.class).set(component));
				}
			}
		});

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
