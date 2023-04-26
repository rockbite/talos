package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.Skin;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.editor.addons.scene.widgets.property.PropertyPanelAssetSelectionWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.SelectBoxWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.scene.components.SpineRendererComponent;

import java.util.function.Supplier;

public class SpineComponentProvider extends RendererComponentProvider<SpineRendererComponent> {

	public SpineComponentProvider (SpineRendererComponent component) {
		super(component);
	}

	@Override
	public Array<PropertyWidget> getListOfProperties () {
		Array<PropertyWidget> properties = new Array<>();

		PropertyPanelAssetSelectionWidget<SkeletonData> atlasWidget = new PropertyPanelAssetSelectionWidget<>("Skeleton", GameAssetType.SKELETON, new Supplier<GameAsset<SkeletonData>>() {
			@Override
			public GameAsset<SkeletonData> get () {
				return component.getGameResource();
			}
		}, new PropertyWidget.ValueChanged<GameAsset<SkeletonData>>() {
			@Override
			public void report (GameAsset<SkeletonData> value) {
				component.setGameAsset(value);
			}
		});

		properties.add(atlasWidget);

		properties.add(WidgetFactory.generate(component, "scale", "Scale"));

		PropertyWidget colorWidget = WidgetFactory.generate(component, "color", "Color");
		properties.add(colorWidget);

		PropertyWidget inheritParentColorWidget = WidgetFactory.generate(component, "shouldInheritParentColor", "Inherit Parent Color");
		properties.add(inheritParentColorWidget);

		properties.add(WidgetFactory.generate(component, "applyAnimation", "Apply Animation"));

		SelectBoxWidget skinSelectWidget = new SelectBoxWidget("Skin", new Supplier<String>() {
			@Override
			public String get () {
				return component.skeleton.getSkin().getName();
			}
		}, new PropertyWidget.ValueChanged<String>() {
			@Override
			public void report (String value) {
				component.setAndUpdateSkin(value);
				SceneUtils.componentUpdated(component.getGameObject().getGameObjectContainerRoot(), component.getGameObject(), component, false);

			}
		}, new Supplier<Array<String>>() {
			@Override
			public Array<String> get () {

				Array<String> names = new Array<>();

				for (Skin skin : component.skeleton.getData().getSkins()) {
					names.add(skin.getName());
				}

				return names;
			}
		});
		properties.add(skinSelectWidget);

		SelectBoxWidget animSelectWidget = new SelectBoxWidget("Animation", new Supplier<String>() {
			@Override
			public String get () {
				if (component.animationState != null && component.animationState.getCurrent(0) != null && component.animationState.getCurrent(0).getAnimation() != null) {
					return component.animationState.getCurrent(0).getAnimation().getName();
				} else {
					return "";
				}
			}
		}, new PropertyWidget.ValueChanged<String>() {
			@Override
			public void report (String value) {
				if (value.equalsIgnoreCase("remove animation")) {
					component.currAnimation = null;
					component.animationState.setEmptyAnimation(0, 0);
					return;
				}
				Animation animation = component.skeleton.getData().findAnimation(value);
				component.animationState.setAnimation(0, animation, true);
				component.currAnimation = value;

				SceneUtils.componentUpdated(component.getGameObject().getGameObjectContainerRoot(), component.getGameObject(), component, false);

			}
		}, new Supplier<Array<String>>() {
			@Override
			public Array<String> get () {
				Array<String> names = new Array<>();
				if (component.skeleton == null || component.skeleton.getData() == null) {
					return names;
				}
				Array<Animation> animations = component.skeleton.getData().getAnimations();
				for (Animation animation : animations) {
					names.add(animation.getName());
				}
				names.add("remove animation");
				return names;
			}
		});
		properties.add(animSelectWidget);

		Array<PropertyWidget> superList = super.getListOfProperties();
		properties.addAll(superList);

		return properties;
	}

	@Override
	public String getPropertyBoxTitle () {
		return "Spine Renderer";
	}

	@Override
	public int getPriority () {
		return 3;
	}

}
