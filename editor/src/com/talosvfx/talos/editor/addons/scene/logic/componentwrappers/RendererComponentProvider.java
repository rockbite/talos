package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.runtime.scene.SceneData;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.SelectBoxWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.SceneLayer;
import com.talosvfx.talos.runtime.scene.components.RendererComponent;

import java.util.function.Supplier;

public abstract class RendererComponentProvider<T extends RendererComponent> extends AComponentProvider<T> {

	public RendererComponentProvider (T component) {
		super(component);
	}

	@Override
	public Array<PropertyWidget> getListOfProperties () {
		Array<PropertyWidget> properties = new Array<>();

		PropertyWidget visibleWidget = WidgetFactory.generate(component, "visible", "Visible");
		PropertyWidget childrenVisibleWidget = WidgetFactory.generate(component, "childrenVisible", "Children Visible");
		PropertyWidget orderingInLayerWidget = WidgetFactory.generate(component, "orderingInLayer", "Ordering");

		SelectBoxWidget layerWidget = new SelectBoxWidget("Sorting Layer", new Supplier<String>() {
			@Override
			public String get () {
				return component.sortingLayer.getName();
			}
		}, new PropertyWidget.ValueChanged<String>() {
			@Override
			public void report (String value) {
				RendererComponent rendererComponent = component;
				rendererComponent.sortingLayer = SharedResources.currentProject.getSceneData().getSceneLayerByName(value);
				GameObject gameObject = rendererComponent.getGameObject();
				SceneUtils.componentUpdated(gameObject.getGameObjectContainerRoot(), gameObject, rendererComponent, false);
			}
		}, new Supplier<Array<String>>() {
			@Override
			public Array<String> get () {
				Array<String> layerNames = new Array<>();
				SceneData sceneData = SharedResources.currentProject.getSceneData();
				Array<SceneLayer> renderLayers = sceneData.getRenderLayers();
				for (SceneLayer renderLayer : renderLayers) {
					layerNames.add(renderLayer.getName());
				}
				return layerNames;
			}
		});

		properties.add(visibleWidget);
		properties.add(childrenVisibleWidget);
		properties.add(orderingInLayerWidget);
		properties.add(layerWidget);

		return properties;
	}

	@Override
	public String getPropertyBoxTitle () {
		return "Map";
	}

	@Override
	public int getPriority () {
		return 2;
	}

}
