package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.Skin;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectCreated;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectDeleted;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectsRestructured;
import com.talosvfx.talos.editor.addons.scene.widgets.property.PropertyPanelAssetSelectionWidget;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.SelectBoxWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectContainer;
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
				GameObject gameObject = component.getGameObject();
				if (!(gameObject != null && gameObject.getGameObjectContainerRoot() != null)) {
					component.setGameAsset(value);
					return;
				}
				GameObjectContainer container = gameObject.getGameObjectContainerRoot();

				// old bone game objects, that must be cleaned up from hierarchy widget.
				ObjectSet<GameObject> oldBoneGOs = new ObjectSet<>();
				oldBoneGOs.addAll(component.directChildrenOfRoot);

				Array<GameObject> childrenToBeBackedUp = new Array<>();
				GameObject.gatherAllChildrenAttachedToBones(gameObject, component.skeleton.getBones(), childrenToBeBackedUp);

				// update asset
				// Will move up the children of old bone gos and populate new bone game objects.
				component.setGameAsset(value);

				ObjectSet<GameObject> restructuredGOs = new ObjectSet<>();
				restructuredGOs.addAll(childrenToBeBackedUp);
				Notifications.fireEvent(Notifications.obtainEvent(GameObjectsRestructured.class).set(container, restructuredGOs));

				// Indicate that bone game objects of old skele were removed for ui to update.
				for (GameObject oldBoneGO : oldBoneGOs) {
					Notifications.fireEvent(Notifications.obtainEvent(GameObjectDeleted.class).set(container, oldBoneGO));
				}

				// Bone game objects may have been created for new skele, tell ui to update.
				for (GameObject directChildOfRootBone : component.directChildrenOfRoot) {
					Notifications.fireEvent(Notifications.obtainEvent(GameObjectCreated.class).set(container, directChildOfRootBone));
				}
			}
		}, component);

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
				if (component.skeleton == null) {
					return "noskin";

				}
				Skin skin = component.skeleton.getSkin();
				if (skin == null) return "noskin";
				return skin.getName();
			}
		}, new PropertyWidget.ValueChanged<String>() {
			@Override
			public void report (String value) {
				if (value.equalsIgnoreCase("noskin")) {
					value = null;
				}
				component.setAndUpdateSkin(value);

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
		}, component);
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
		}, component);
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
