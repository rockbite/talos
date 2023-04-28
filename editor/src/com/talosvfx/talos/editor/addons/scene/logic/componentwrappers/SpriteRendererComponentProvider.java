package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.editor.addons.scene.widgets.property.PropertyPanelAssetSelectionWidget;
import com.talosvfx.talos.editor.nodes.widgets.ValueWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.Vector2PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.components.SpriteRendererComponent;

import java.util.function.Supplier;

public final class SpriteRendererComponentProvider extends RendererComponentProvider<SpriteRendererComponent> {

	private Vector2PropertyWidget sizeWidget;

	public SpriteRendererComponentProvider (SpriteRendererComponent component) {
		super(component);
	}

	@Override
	public Array<PropertyWidget> getListOfProperties () {
		Array<PropertyWidget> properties = new Array<>();

		//TODO aspect ratio shit is broken again now
		PropertyWidget textureWidget = WidgetFactory.generateForGameAsset(component, "gameAsset", null, "Texture", GameAssetType.SPRITE);

		PropertyWidget colorWidget = WidgetFactory.generate(component, "color", "Color");
		PropertyWidget inheritParentColorWidget = WidgetFactory.generate(component, "shouldInheritParentColor", "Inherit Parent Color");
		PropertyWidget flipXWidget = WidgetFactory.generate(component, "flipX", "Flip X");
		PropertyWidget flipYWidget = WidgetFactory.generate(component, "flipY", "Flip Y");
		PropertyWidget fixAspectRatioWidget = WidgetFactory.generate(component, "fixAspectRatio", "Fix Aspect Ratio");
		PropertyWidget renderModesWidget = WidgetFactory.generate(component, "renderMode", "Render Mode");
		sizeWidget = (Vector2PropertyWidget) WidgetFactory.generate(component, "size", "Size");
		PropertyWidget tileSizeWidget = WidgetFactory.generate(component, "tileSize", "Tile Size");

		renderModesWidget.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				if (component.renderMode == SpriteRendererComponent.RenderMode.tiled) {
					tileSizeWidget.setVisible(true);
				} else {
					tileSizeWidget.setVisible(false);
				}
			}
		});

		// snap to aspect ratio
		fixAspectRatioWidget.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				snapToAspectRatio(sizeWidget);
			}
		});

		// change size by aspect ratio if aspect ratio is fixed
		sizeWidget.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				if (!component.shouldFixAspectRatio(true))
					return;

				if (event.getTarget() instanceof ValueWidget) {
					final Vector2PropertyWidget vector2PropertyWidget = sizeWidget;
					final ValueWidget xValue = vector2PropertyWidget.xValue;
					final ValueWidget yValue = vector2PropertyWidget.yValue;
					final AtlasSprite texture = component.getGameResource().getResource();

					if (texture != null) {
						final float aspect = texture.getRegionHeight() * 1f / texture.getRegionWidth();

						if (event.getTarget() == xValue) {
							component.size.y = component.size.x * aspect;
						}

						if (event.getTarget() == yValue) {
							component.size.x = component.size.y / aspect;
						}
					}

					xValue.setValue(component.size.x, false);
					yValue.setValue(component.size.y, false);
				}
			}
		});

		properties.add(textureWidget);
		properties.add(colorWidget);
		properties.add(inheritParentColorWidget);
		properties.add(fixAspectRatioWidget);
		properties.add(flipXWidget);
		properties.add(flipYWidget);
		properties.add(renderModesWidget);

		Array<PropertyWidget> superList = super.getListOfProperties();
		properties.addAll(superList);
		properties.add(sizeWidget);
		properties.add(tileSizeWidget);

		return properties;
	}

	private void snapToAspectRatio(Vector2PropertyWidget sizeWidget) {
		if (!component.shouldFixAspectRatio(true))
			return;

		final AtlasSprite texture = component.getGameResource().getResource();

		if (texture != null) {
			final float aspect = texture.getRegionHeight() * 1f / texture.getRegionWidth();
			component.size.y = component.size.x * aspect;
		}

		final ValueWidget yValue = sizeWidget.yValue;
		yValue.setValue(component.size.y, false);
	}

	@Override
	public String getPropertyBoxTitle () {
		return "Sprite Renderer";
	}

	@Override
	public int getPriority () {
		return 2;
	}

	public void applyConstrains() {
		if (!component.shouldFixAspectRatio(true))
			return;

		final Vector2PropertyWidget vector2PropertyWidget = ((Vector2PropertyWidget)sizeWidget);
		final ValueWidget xValue = vector2PropertyWidget.xValue;
		final ValueWidget yValue = vector2PropertyWidget.yValue;
		final AtlasSprite texture = component.getGameResource().getResource();

		if (texture != null) {
			final float aspect = texture.getRegionHeight() * 1f / texture.getRegionWidth();
			if (sizeWidget.xValue.isChanged(component.size.x)) {
				component.size.y = component.size.x * aspect;
			}

			if (sizeWidget.yValue.isChanged(component.size.y)) {
				component.size.x = component.size.y / aspect;
			}
		}

		xValue.setValue(component.size.x, false);
		yValue.setValue(component.size.y, false);
	}
}
