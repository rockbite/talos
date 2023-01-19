package com.talosvfx.talos.editor.addons.scene.logic.metawrappers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.assets.meta.SpineMetadata;

import java.util.function.Supplier;

public class SpineMetaDataProvider extends AMetaDataProvider<SpineMetadata> {

	public SpineMetaDataProvider (SpineMetadata meta) {
		super(meta);
	}

	@Override
	public Array<PropertyWidget> getListOfProperties () {
		Array<PropertyWidget> propertyWidgets = new Array<>();

		PropertyWidget pixelPerUnit = WidgetFactory.generate(meta, "pixelsPerUnit", "pxToWorld");
		pixelPerUnit.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				//Reload the game asset with new import scale
				AssetRepository.getInstance().reloadGameAssetForRawFile(meta.link);
			}
		});
		propertyWidgets.add(pixelPerUnit);

		AssetSelectWidget atlasWidget = new AssetSelectWidget("Atlas", GameAssetType.ATLAS, new Supplier<String>() {
			@Override
			public String get() {
				return meta.atlasPath;
			}
		}, new PropertyWidget.ValueChanged<String>() {
			@Override
			public void report(String value) {
				meta.atlasPath = value;
			}
		});
		propertyWidgets.add(atlasWidget);

		return propertyWidgets;
	}

	@Override
	public String getPropertyBoxTitle () {
		return "Skeleton data";
	}
}
