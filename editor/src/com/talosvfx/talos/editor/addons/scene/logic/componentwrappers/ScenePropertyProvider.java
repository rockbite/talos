package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.TalosProjectData;
import com.talosvfx.talos.editor.widgets.propertyWidgets.CheckboxWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.SelectBoxWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;
import com.talosvfx.talos.runtime.scene.SceneData;
import com.talosvfx.talos.editor.widgets.propertyWidgets.DynamicItemListWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.SceneLayerWrapper;
import com.talosvfx.talos.editor.widgets.propertyWidgets.LabelWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.runtime.scene.Scene;
import com.talosvfx.talos.runtime.scene.SceneLayer;
import com.talosvfx.talos.runtime.scene.render.RenderStrategy;

import com.talosvfx.talos.runtime.utils.Supplier;

public class ScenePropertyProvider implements IPropertyProvider {

	private final Scene scene;
	private SelectBoxWidget renderStrategy;
	private SceneLayer selectedLayer;

	public ScenePropertyProvider (Scene scene) {
		this.scene = scene;
	}

	@Override
	public Array<PropertyWidget> getListOfProperties () {
		selectedLayer = SharedResources.currentProject.getSceneData().getPreferredSceneLayer();

		Array<PropertyWidget> properties = new Array<>();

		LabelWidget labelWidget = new LabelWidget("Name", new Supplier<String>() {
			@Override
			public String get () {
				return scene.getName();
			}
		}, scene);

		PropertyWidget checkboxWidget = WidgetFactory.generate(scene, "optimized", "Optimizer scene");

		Supplier<SceneLayerWrapper> newItemDataSupplier = new Supplier<SceneLayerWrapper>() {
			@Override
			public SceneLayerWrapper get () {
				String base = "NewLayer";
				String newLayer = getNextAvailableLayerName(base);
				SceneData sceneData = SharedResources.currentProject.getSceneData();
				Array<SceneLayer> renderLayers = sceneData.getRenderLayers();
				SceneLayer sceneLayer = new SceneLayer(newLayer, renderLayers.size);
				renderLayers.add(sceneLayer);
				return new SceneLayerWrapper(sceneLayer);
			}
		};
		DynamicItemListWidget<SceneLayerWrapper> itemListWidget = new DynamicItemListWidget<SceneLayerWrapper>("Layers", new Supplier<Array<SceneLayerWrapper>>() {
			@Override
			public Array<SceneLayerWrapper> get () {
				TalosProjectData currentProject = SharedResources.currentProject;
				SceneData sceneData = currentProject.getSceneData();
				Array<SceneLayer> renderLayers = sceneData.getRenderLayers();
				Array<SceneLayerWrapper> sceneLayerWrappers = new Array<>();
				for (SceneLayer renderLayer : renderLayers) {
					SceneLayerWrapper sceneLayerWrapper = new SceneLayerWrapper(renderLayer);
					sceneLayerWrappers.add(sceneLayerWrapper);
				}

				return sceneLayerWrappers;
			}
		}, new PropertyWidget.ValueChanged<Array<SceneLayerWrapper>>() {
			@Override
			public void report (Array<SceneLayerWrapper> value) {
				SceneData sceneData = SharedResources.currentProject.getSceneData();
				Array<SceneLayer> renderLayers = sceneData.getRenderLayers();
				renderLayers.clear();
				for (int i = 0; i < value.size; i++) {
					SceneLayerWrapper sceneLayerWrapper = value.get(i);
					SceneLayer instance = sceneLayerWrapper.getInstance();
					instance.setIndex(i);
					renderLayers.add(instance);
				}
			}
		}, scene) {
			@Override
			public boolean canDelete (SceneLayerWrapper itemData) {
				return itemData.canDelete();
			}
		};


		itemListWidget.setInteraction(new DynamicItemListWidget.DynamicItemListInteraction<SceneLayerWrapper>() {
			@Override
			public Supplier<SceneLayerWrapper> newInstanceCreator () {
				return newItemDataSupplier;
			}

			@Override
			public String getID (SceneLayerWrapper o) {
				return o.getID().toString();
			}

			@Override
			public String updateName (SceneLayerWrapper itemData, String newText) {
				newText = getNextAvailableLayerName(newText);
				itemData.updateName(newText);
				return newText;
			}

			@Override
			public void onUpdate() {
				SceneData sceneData = SharedResources.currentProject.getSceneData();
				int preferredLayerIndex = sceneData.getPreferredSceneLayer().getIndex();
				itemListWidget.list.addNodeToSelectionByIndex(preferredLayerIndex);

				setSelectedLayer(itemListWidget.list.getSelection().first().getObject().getInstance());
			}

			@Override
			public void onDeleteNode(SceneLayerWrapper itemData) {
				SceneData sceneData = SharedResources.currentProject.getSceneData();
				Array<SceneLayer> renderLayers = sceneData.getRenderLayers();
				boolean removed = renderLayers.removeValue(itemData.getInstance(), true);
				if (removed) {
					SceneUtils.layersUpdated();

					if (sceneData.getPreferredSceneLayer() == itemData.getInstance()) {
						sceneData.setPreferredSceneLayer("Default");
						int preferredLayerIndex = sceneData.getPreferredSceneLayer().getIndex();
						itemListWidget.list.addNodeToSelectionByIndex(preferredLayerIndex);
					}
				}
			}
		});
		itemListWidget.list.addItemListener(new FilteredTree.ItemListener<SceneLayerWrapper>() {
			@Override
			public void selected (FilteredTree.Node<SceneLayerWrapper> node) {
				super.selected(node);
				setSelectedLayer(node.getObject().getInstance());

				SharedResources.currentProject.getSceneData().setPreferredSceneLayer(node.getName());
			}
		});

		itemListWidget.setDraggableInLayerOnly(true);


		properties.add(labelWidget);
		properties.add(checkboxWidget);
		properties.add(itemListWidget);

		itemListWidget.list.addItemListener(new FilteredTree.ItemListener<SceneLayerWrapper>() {
			@Override
			public void selected (FilteredTree.Node<SceneLayerWrapper> node) {
				super.selected(node);
				selectedLayer = node.getObject().getInstance();
			}
		});

		renderStrategy = new SelectBoxWidget("Layer render mode", new Supplier<String>() {
			@Override
			public String get () {
				return selectedLayer.getRenderStrategy().name();
			}
		}, new PropertyWidget.ValueChanged<String>() {
			@Override
			public void report (String value) {
				RenderStrategy startToSet = RenderStrategy.valueOf(value);
				selectedLayer.setRenderStrategy(startToSet);
			}
		}, new Supplier<Array<String>>() {
			@Override
			public Array<String> get () {
				Array<String> options = new Array<>();
				for (RenderStrategy value : RenderStrategy.values()) {
					options.add(value.name());
				}
				return options;
			}
		}, scene);

		properties.add(renderStrategy);


		return properties;
	}

	private void setSelectedLayer (SceneLayer sceneLayer) {
		selectedLayer = sceneLayer;
		renderStrategy.updateValue();
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
