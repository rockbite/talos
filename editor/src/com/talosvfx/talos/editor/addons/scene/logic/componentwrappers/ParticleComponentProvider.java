package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.widgets.property.PropertyPanelAssetSelectionWidget;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ButtonPropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.assets.RawAsset;
import com.talosvfx.talos.runtime.scene.components.ParticleComponent;
import com.talosvfx.talos.runtime.vfx.serialization.BaseVFXProjectData;

import java.util.function.Supplier;

public class ParticleComponentProvider<T extends BaseVFXProjectData> extends RendererComponentProvider<ParticleComponent<T>> {

	public ParticleComponentProvider (ParticleComponent<T> component) {
		super(component);
	}

	@Override
	public Array<PropertyWidget> getListOfProperties () {
		Array<PropertyWidget> properties = new Array<>();

		//		if (this.descriptor != vfxProjectData.getDescriptorSupplier().get()) {
		//			this.descriptor = vfxProjectData.getDescriptorSupplier().get();
		//			this.effectInstance = this.descriptor.createEffectInstance();
		//		}

		PropertyPanelAssetSelectionWidget<T> descriptorWidget = new PropertyPanelAssetSelectionWidget<>("Effect", GameAssetType.VFX, new Supplier<GameAsset<T>>() {
			@Override
			public GameAsset<T> get() {
				return component.gameAsset;
			}
		}, new PropertyWidget.ValueChanged<GameAsset<T>>() {
			@Override
			public void report(GameAsset<T> value) {
				component.setGameAsset(value);
			}
		});

		ButtonPropertyWidget<String> linkedToWidget = new ButtonPropertyWidget<String>("Effect Project", "Edit", new ButtonPropertyWidget.ButtonListener<String>() {
			@Override
			public void clicked(ButtonPropertyWidget<String> widget) {
				//Edit this tls
				if (component.gameAsset != null) {
					SharedResources.appManager.openNewAsset(component.gameAsset);
				}
			}
		}, new Supplier<String>() {
			@Override
			public String get() {
//                return linkedTo;
				return "";
			}
		}, new PropertyWidget.ValueChanged<String>() {
			@Override
			public void report(String value) {
//                linkedTo = value;
			}
		});

		properties.add(descriptorWidget);
		properties.add(linkedToWidget);
		properties.addAll(super.getListOfProperties());

		return properties;
	}
	@Override
	public String getPropertyBoxTitle () {
		return "Particle Effect";
	}

	@Override
	public int getPriority () {
		return 2;
	}
}
