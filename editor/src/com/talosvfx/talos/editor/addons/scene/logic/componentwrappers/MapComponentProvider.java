package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.editor.addons.scene.events.TalosLayerSelectEvent;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.widgets.propertyWidgets.DynamicItemListWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.TalosLayerPropertiesWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;
import com.talosvfx.talos.runtime.maps.TalosLayer;
import com.talosvfx.talos.runtime.scene.components.MapComponent;

import com.talosvfx.talos.runtime.utils.Supplier;

public class MapComponentProvider extends RendererComponentProvider<MapComponent> {


	public transient TalosLayer selectedLayer;
	private TalosLayerPropertiesWidget talosLayerPropertiesWidget;
	private DynamicItemListWidget<TalosLayer> itemListWidget;

	public MapComponentProvider (MapComponent component) {
		super(component);
	}

	@Override
	public Array<PropertyWidget> getListOfProperties () {

		Array<PropertyWidget> properties = super.getListOfProperties();

		Supplier<TalosLayer> supplier = new Supplier<TalosLayer>() {
			@Override
			public TalosLayer get () {
				return new TalosLayer("NewLayer");
			}
		};
		itemListWidget = new DynamicItemListWidget<>("Layers", new Supplier<Array<TalosLayer>>() {
			@Override
			public Array<TalosLayer> get () {
				return component.getLayers();
			}
		}, new PropertyWidget.ValueChanged<Array<TalosLayer>>() {
			@Override
			public void report (Array<TalosLayer> value) {
				component.getLayers().clear();
				for (TalosLayer item : value) {
					component.getLayers().add(item);
				}
				SceneUtils.layersUpdated();
			}
		}, new DynamicItemListWidget.DynamicItemListInteraction<TalosLayer>() {
			@Override
			public Supplier<TalosLayer> newInstanceCreator () {

				return supplier;
			}

			@Override
			public String getID (TalosLayer o) {
				return o.getName();
			}

			@Override
			public String updateName (TalosLayer talosLayer, String newText) {
				talosLayer.setName(newText);
				return newText;
			}

			@Override
			public void onUpdate() {

			}

			@Override
			public void onDeleteNode(TalosLayer talosLayer) {

			}
		}, component);

		talosLayerPropertiesWidget = new TalosLayerPropertiesWidget(null, new Supplier<TalosLayer>() {
			@Override
			public TalosLayer get () {
				return component.selectedLayer;
			}
		}, new PropertyWidget.ValueChanged<TalosLayer>() {
			@Override
			public void report (TalosLayer value) {
			}
		}, component);
		itemListWidget.list.addItemListener(new FilteredTree.ItemListener<TalosLayer>() {
			@Override
			public void selected (FilteredTree.Node<TalosLayer> node) {
				super.selected(node);
				setLayerSelected(node.getObject());
			}
		});

		properties.add(WidgetFactory.generate(component, "mapType", "Type"));
		properties.add(itemListWidget);
		properties.add(talosLayerPropertiesWidget);

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

	public void setLayerSelectedByEmulating (TalosLayer layer) {
		Array<FilteredTree.Node<TalosLayer>> rootNodes = itemListWidget.list.getRootNodes();
		for (FilteredTree.Node<TalosLayer> node : rootNodes) {
			if (node.getObject() == layer) {
				itemListWidget.list.getSelection().set(node);
				break;
			}
		}

		setLayerSelected(layer);
	}

	public void setLayerSelected (TalosLayer layer) {
		selectedLayer = layer;
		TalosLayerSelectEvent talosLayerSelectEvent = Notifications.obtainEvent(TalosLayerSelectEvent.class);
		talosLayerSelectEvent.layer = selectedLayer;
		Notifications.fireEvent(talosLayerSelectEvent);
		talosLayerPropertiesWidget.updateWidget(layer);
		talosLayerPropertiesWidget.toggleHide(false);
	}

}
