package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.TalosProjectData;
import com.talosvfx.talos.editor.project2.projectdata.SceneData;
import com.talosvfx.talos.editor.widgets.propertyWidgets.DynamicItemListWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ItemData;
import com.talosvfx.talos.editor.widgets.propertyWidgets.LabelWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.runtime.scene.Scene;
import com.talosvfx.talos.runtime.scene.SceneLayer;

import java.util.function.Supplier;

public class ScenePropertyProvider implements IPropertyProvider {

	private final Scene scene;

	public ScenePropertyProvider (Scene scene) {
		this.scene = scene;
	}

	@Override
	public Array<PropertyWidget> getListOfProperties () {
		Array<PropertyWidget> properties = new Array<>();

		LabelWidget labelWidget = new LabelWidget("Name", new Supplier<String>() {
			@Override
			public String get () {
				return scene.getName();
			}
		});

		Supplier<ItemData> newItemDataSupplier = new Supplier<ItemData>() {
			@Override
			public ItemData get () {
				String base = "NewLayer";
				String newLayer = getNextAvailableLayerName(base);

				return new ItemData(newLayer, newLayer);
			}
		};
		DynamicItemListWidget<ItemData> itemListWidget = new DynamicItemListWidget<ItemData>("Layers", new Supplier<Array<ItemData>>() {
			@Override
			public Array<ItemData> get () {
				Array<ItemData> list = new Array<>();
				TalosProjectData currentProject = SharedResources.currentProject;
				Array<SceneLayer> renderLayers = currentProject.getSceneData().getRenderLayers();
				for (SceneLayer layer : renderLayers) {
					ItemData itemData = new ItemData(layer.getName());
					if (layer.getName().equals("Default")) {
						itemData.canDelete = false;
					}
					list.add(itemData);
				}
				return list;
			}
		}, new PropertyWidget.ValueChanged<Array<ItemData>>() {
			@Override
			public void report (Array<ItemData> value) {
				TalosProjectData currentProject = SharedResources.currentProject;
				Array<SceneLayer> renderLayers = currentProject.getSceneData().getRenderLayers();
				renderLayers.clear();
				int i = 0;
				for (ItemData item : value) {
					SceneLayer sceneLayer = new SceneLayer(item.text, i++);
					renderLayers.add(sceneLayer);
				}
				SceneUtils.layersUpdated();
			}
		}, new DynamicItemListWidget.DynamicItemListInteraction<ItemData>() {
			@Override
			public Supplier<ItemData> newInstanceCreator () {
				return newItemDataSupplier;
			}

			@Override
			public String getID (ItemData o) {
				return o.id;
			}

			@Override
			public String updateName (ItemData itemData, String newText) {
				newText = getNextAvailableLayerName(newText);
				itemData.updateName(newText);
				return newText;
			}
		}) {
			@Override
			public boolean canDelete (ItemData itemData) {
				return itemData.canDelete;
			}
		};

		itemListWidget.setDraggableInLayerOnly(true);

		properties.add(labelWidget);
		properties.add(itemListWidget);

		return properties;
	}

	@Override
	public String getPropertyBoxTitle () {
		return "Scene Properties";
	}

	@Override
	public int getPriority () {
		return 0;
	}

	@Override
	public Class<? extends IPropertyProvider> getType () {
		return getClass();
	}

	private String getNextAvailableLayerName (String base) {
		String newLayer = base;
		int count = 1;
		SceneData sceneData = SharedResources.currentProject.getSceneData();
		Array<SceneLayer> renderLayers = sceneData.getRenderLayers();
		Array<String> layerNames = new Array<>();
		for (SceneLayer renderLayer : renderLayers) {
			layerNames.add(renderLayer.getName());
		}
		while (layerNames.contains(newLayer, false)) {
			newLayer = base + count++;
		}

		return newLayer;
	}
}
