package com.talosvfx.talos.editor.addons.scene.logic.metawrappers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.apps.spriteeditor.SpriteEditor;
import com.talosvfx.talos.editor.addons.scene.apps.spriteeditor.SpriteEditorApp;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.events.SpritePixelPerUnitUpdateEvent;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ButtonPropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.meta.SpriteMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpriteMetaDataProvider extends AMetaDataProvider<SpriteMetadata> {

	private static final Logger logger = LoggerFactory.getLogger(SpriteMetaDataProvider.class);

	public SpriteMetaDataProvider (SpriteMetadata meta) {
		super(meta);
	}

	@Override
	public Array<PropertyWidget> getListOfProperties () {
		Array<PropertyWidget> propertyWidgets = new Array<>();

		final PropertyWidget pixelToWorldPropertyWidget = WidgetFactory.generate(meta, "pixelsPerUnit", "pxToWorld");
		pixelToWorldPropertyWidget.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Notifications.fireEvent(Notifications.obtainEvent(SpritePixelPerUnitUpdateEvent.class).setSpriteMetadata(meta));
			}
		});
		propertyWidgets.add(pixelToWorldPropertyWidget);
		propertyWidgets.add(WidgetFactory.generate(meta, "minFilter", "MinFilter"));
		propertyWidgets.add(WidgetFactory.generate(meta, "magFilter", "MagFilter"));

		ButtonPropertyWidget<String> spriteEditor = new ButtonPropertyWidget<String>("Sprite Editor", new ButtonPropertyWidget.ButtonListener<String>() {
			@Override
			public void clicked (ButtonPropertyWidget<String> widget) {
				logger.info("todo open sprite editor request");
				GameAsset<AtlasRegion> assetForPath = (GameAsset<AtlasRegion>)AssetRepository.getInstance().getAssetForPath(meta.link.handle, false);
				SharedResources.appManager.openApp(assetForPath, SpriteEditorApp.class);
			}
		});
		propertyWidgets.add(spriteEditor);

		return propertyWidgets;
	}

	@Override
	public String getPropertyBoxTitle () {
		return "Sprite";
	}
}
