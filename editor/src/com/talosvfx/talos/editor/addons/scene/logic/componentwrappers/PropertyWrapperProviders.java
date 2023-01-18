package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Constructor;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.SkeletonData;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectActiveChanged;
import com.talosvfx.talos.editor.addons.scene.events.TalosLayerSelectEvent;
import com.talosvfx.talos.editor.addons.scene.events.commands.GONameChangeCommand;
import com.talosvfx.talos.editor.addons.scene.logic.IPropertyHolder;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.data.RoutineStageData;
import com.talosvfx.talos.editor.nodes.widgets.ValueWidget;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.TalosProjectData;
import com.talosvfx.talos.editor.project2.projectdata.SceneData;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ButtonPropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.CheckboxWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.DynamicItemListWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.EditableLabelWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ItemData;
import com.talosvfx.talos.editor.widgets.propertyWidgets.LabelWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.SelectBoxWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.TalosLayerPropertiesWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.Vector2PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.maps.GridPosition;
import com.talosvfx.talos.runtime.maps.TalosLayer;
import com.talosvfx.talos.runtime.routine.serialization.BaseRoutineData;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.Scene;
import com.talosvfx.talos.runtime.scene.SceneLayer;
import com.talosvfx.talos.runtime.scene.components.AComponent;
import com.talosvfx.talos.runtime.scene.components.CameraComponent;
import com.talosvfx.talos.runtime.scene.components.CurveComponent;
import com.talosvfx.talos.runtime.scene.components.MapComponent;
import com.talosvfx.talos.runtime.scene.components.PaintSurfaceComponent;
import com.talosvfx.talos.runtime.scene.components.ParticleComponent;
import com.talosvfx.talos.runtime.scene.components.RendererComponent;
import com.talosvfx.talos.runtime.scene.components.RoutineRendererComponent;
import com.talosvfx.talos.runtime.scene.components.ScriptComponent;
import com.talosvfx.talos.runtime.scene.components.SpineRendererComponent;
import com.talosvfx.talos.runtime.scene.components.SpriteRendererComponent;
import com.talosvfx.talos.runtime.scene.components.TileDataComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;
import com.talosvfx.talos.runtime.vfx.serialization.BaseVFXProjectData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class PropertyWrapperProviders {

	private static final Logger logger = LoggerFactory.getLogger(PropertyWrapperProviders.class);

	public static ObjectMap<Class<?>, Class<? extends IPropertyHolder>> propertyHoldersForClass = new ObjectMap<>();
	public static ObjectMap<Class<?>, Class<? extends IPropertyProvider>> propertyProvidersForClass = new ObjectMap<>();


	static {
		propertyHoldersForClass.put(Scene.class, ScenePropertyHolder.class);
		propertyHoldersForClass.put(GameObject.class, GameObjectPropertyHolder.class);

		propertyProvidersForClass.put(Scene.class, ScenePropertyProvider.class);
		propertyProvidersForClass.put(GameObject.class, GameObjectPropertyProvider.class);
	}

	private static ObjectMap<Object, IPropertyHolder> propertyHolderCache = new ObjectMap<>();
	private static ObjectMap<Object, IPropertyProvider> propertyProviderCache = new ObjectMap<>();

	public static IPropertyHolder getOrCreateHolder (Object object) {
		if (propertyHolderCache.containsKey(object)) {
			return propertyHolderCache.get(object);
		}
		Class<?> clazz = object.getClass();
		if (propertyHoldersForClass.containsKey(clazz)) {
			try {
				Class<? extends IPropertyHolder> iPropertyProvider = propertyHoldersForClass.get(clazz);
				Constructor constructor = ClassReflection.getConstructor(iPropertyProvider, clazz);
				return (IPropertyHolder)constructor.newInstance(object);
			} catch (ReflectionException e) {
				throw new RuntimeException(e);
			}
		} else {
			logger.error("No such property provider for type: {}", clazz);
			throw new GdxRuntimeException("Holder must be registered in PropertyWrapperProviders");
		}
	}

	public static IPropertyProvider getOrCreateProvider (Object object) {
		if (propertyProviderCache.containsKey(object)) {
			return propertyProviderCache.get(object);
		}
		Class<?> clazz = object.getClass();
		if (propertyProvidersForClass.containsKey(clazz)) {
			try {
				Class<? extends IPropertyProvider> iPropertyProvider = propertyProvidersForClass.get(clazz);
				Constructor constructor = ClassReflection.getConstructor(iPropertyProvider, clazz);
				return (IPropertyProvider)constructor.newInstance(object);
			} catch (ReflectionException e) {
				throw new RuntimeException(e);
			}
		} else {
			logger.error("No such property provider for type: {}", clazz);
			throw new GdxRuntimeException("Provider must be registered in PropertyWrapperProviders");
		}
	}


	public abstract static class ObjectPropertyHolder<T> implements IPropertyHolder {

	}

	public static class GameObjectPropertyHolder extends ObjectPropertyHolder<GameObject> {

		private final GameObject gameObject;
		private final GameObjectPropertyProvider gameObjectPropertyProvider;

		public GameObjectPropertyHolder (GameObject gameObject) {
			this.gameObject = gameObject;
			gameObjectPropertyProvider = new GameObjectPropertyProvider(gameObject);
		}

		@Override
		public Iterable<IPropertyProvider> getPropertyProviders () {
			Array<IPropertyProvider> list = new Array<>();

			list.add(gameObjectPropertyProvider);

			for (AComponent component: gameObject.getComponents()) {
				list.add(PropertyWrapperProviders.getOrCreateProvider(component));
			}

			return list;
		}
		@Override
		public String getName () {
			return null;
		}
	}



	public static class ScenePropertyHolder extends ObjectPropertyHolder<Scene> {

		private final Scene scene;
		private final ScenePropertyProvider scenePropertyProvider;

		public ScenePropertyHolder (Scene scene) {
			this.scene = scene;
			scenePropertyProvider = new ScenePropertyProvider(scene);
		}

		@Override
		public Iterable<IPropertyProvider> getPropertyProviders () {
			Array<IPropertyProvider> list = new Array<>();

			list.add(scenePropertyProvider);

			return list;
		}

		@Override
		public String getName () {
			return null;
		}
	}

	public abstract static class ComponentPropertyProvider<T extends AComponent> implements IPropertyProvider {

		protected final T component;

		public ComponentPropertyProvider (T component) {
			this.component = component;
		}

	}

	private abstract static class AComponentProvider<T extends AComponent> extends ComponentPropertyProvider<T> {

		public AComponentProvider (T component) {
			super(component);
		}

		@Override
		public Class<? extends IPropertyProvider> getType () {
			return getClass();
		}
	}

	private static class CameraComponentProvider extends ComponentPropertyProvider<CameraComponent> {

		public CameraComponentProvider (CameraComponent component) {
			super(component);
		}

		@Override
		public Array<PropertyWidget> getListOfProperties () {
			Array<PropertyWidget> properties = new Array<>();

			PropertyWidget colorWidget = WidgetFactory.generate(this, "backgroundColor", "Background Color");
			PropertyWidget zoomWidget = WidgetFactory.generate(this, "zoom", "Zoom");
			PropertyWidget sizeWidget = WidgetFactory.generate(this, "size", "Size");

			properties.add(colorWidget);
			properties.add(zoomWidget);
			properties.add(sizeWidget);

			return properties;
		}

		@Override
		public String getPropertyBoxTitle () {
			return "Camera";
		}

		@Override
		public int getPriority () {
			return 2;
		}

		@Override
		public Class<? extends IPropertyProvider> getType () {
			return getClass();
		}
	}

	private static class CurveComponentProvider extends AComponentProvider<CurveComponent> {

		public CurveComponentProvider (CurveComponent component) {
			super(component);
		}

		@Override
		public Array<PropertyWidget> getListOfProperties () {
			Array<PropertyWidget> properties = new Array<>();

			properties.add(new LabelWidget("segments", new Supplier<String>() {
				@Override
				public String get () {
					return component.getNumSegments() + "";
				}
			}));

			properties.add(new LabelWidget("points", new Supplier<String>() {
				@Override
				public String get () {
					return component.points.size + "";
				}
			}));

			ButtonPropertyWidget<String> cleanButton = new ButtonPropertyWidget<String>("Create New", new ButtonPropertyWidget.ButtonListener() {
				@Override
				public void clicked (ButtonPropertyWidget widget) {
					component.setToNew();
				}
			});

			properties.add(cleanButton);

			CheckboxWidget isClosedWidget = new CheckboxWidget("Toggle Closed", new Supplier<Boolean>() {
				@Override
				public Boolean get () {
					return component.isClosed;
				}
			}, new PropertyWidget.ValueChanged<Boolean>() {
				@Override
				public void report (Boolean value) {
					if (component.isClosed != value) {
						component.setClosedState(value);
					}
				}
			});

			properties.add(cleanButton);
			properties.add(isClosedWidget);

			CheckboxWidget autoSetWidget = new CheckboxWidget("Automatic Control", new Supplier<Boolean>() {
				@Override
				public Boolean get () {
					return component.automaticControl;
				}
			}, new PropertyWidget.ValueChanged<Boolean>() {
				@Override
				public void report (Boolean value) {
					if (component.automaticControl != value) {
						component.automaticControl = value;
						if (component.automaticControl) {
							component.autoSetAllControlPoints();
						}
					}
				}
			});
			properties.add(autoSetWidget);

			return properties;
		}

		@Override
		public String getPropertyBoxTitle () {
			return "Curve Component";
		}

		@Override
		public int getPriority () {
			return 2;
		}
	}

	private static class MapComponentProvider extends RendererComponentProvider<MapComponent> {

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
			});

			talosLayerPropertiesWidget = new TalosLayerPropertiesWidget(null, new Supplier<TalosLayer>() {
				@Override
				public TalosLayer get () {
					return component.selectedLayer;
				}
			}, new PropertyWidget.ValueChanged<TalosLayer>() {
				@Override
				public void report (TalosLayer value) {
				}
			});
			itemListWidget.list.addItemListener(new FilteredTree.ItemListener<TalosLayer>() {
				@Override
				public void selected (FilteredTree.Node<TalosLayer> node) {
					super.selected(node);
					setLayerSelected(node.getObject());
				}
			});

			properties.add(WidgetFactory.generate(this, "mapType", "Type"));
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

	private abstract static class RendererComponentProvider<T extends RendererComponent> extends AComponentProvider<T> {

		public RendererComponentProvider (T component) {
			super(component);
		}

		@Override
		public Array<PropertyWidget> getListOfProperties () {
			Array<PropertyWidget> properties = new Array<>();

			PropertyWidget visibleWidget = WidgetFactory.generate(this, "visible", "Visible");
			PropertyWidget childrenVisibleWidget = WidgetFactory.generate(this, "childrenVisible", "Children Visible");
			PropertyWidget orderingInLayerWidget = WidgetFactory.generate(this, "orderingInLayer", "Ordering");

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


	private static class PaintSurfaceComponentProvider extends AComponentProvider<PaintSurfaceComponent> {

		public PaintSurfaceComponentProvider (PaintSurfaceComponent component) {
			super(component);
		}

		@Override
		public Array<PropertyWidget> getListOfProperties() {

			Array<PropertyWidget> properties = new Array<>();

			AssetSelectWidget<Texture> textureWidget = new AssetSelectWidget<>("Texture", GameAssetType.SPRITE, new Supplier<GameAsset<Texture>>() {
				@Override
				public GameAsset<Texture> get() {
					return component.gameAsset;
				}
			}, new PropertyWidget.ValueChanged<GameAsset<Texture>>() {
				@Override
				public void report(GameAsset<Texture> value) {
					component.setGameAsset(value);
				}
			});

			PropertyWidget sizeWidget = WidgetFactory.generate(this, "size", "Size");

			PropertyWidget overlayWidget = WidgetFactory.generate(this, "overlay", "Overlay");

			PropertyWidget redChannelWidget = WidgetFactory.generate(this, "redChannel", "Red Channel");
			PropertyWidget greenChannelWidget = WidgetFactory.generate(this, "greenChannel", "Green Channel");
			PropertyWidget blueChannelWidget = WidgetFactory.generate(this, "blueChannel", "Blue Channel");
			PropertyWidget alphaChannelWidget = WidgetFactory.generate(this, "alphaChannel", "Alpha Channel");

			properties.add(textureWidget);
			properties.add(sizeWidget);
			properties.add(overlayWidget);

			properties.add(redChannelWidget);
			properties.add(greenChannelWidget);
			properties.add(blueChannelWidget);
			properties.add(alphaChannelWidget);

			return properties;
		}

		@Override
		public String getPropertyBoxTitle() {
			return "Paint Surface";
		}

		@Override
		public int getPriority() {
			return 4;
		}
	}

	private static class ParticleComponentProvider extends RendererComponentProvider<ParticleComponent<? extends BaseVFXProjectData>> {

		public ParticleComponentProvider (ParticleComponent<? extends BaseVFXProjectData> component) {
			super(component);
		}
	}

	private static class RoutineRenderComponentProvider extends RendererComponentProvider<RoutineRendererComponent> {

		Array<PropertyWidget> properties = new Array<>();
		public Array<PropertyWrapper<?>> propertyWrappers = new Array<>();

		public RoutineRenderComponentProvider (RoutineRendererComponent component) {
			super(component);
		}

		@Override
		public Array<PropertyWidget> getListOfProperties() {
			properties.clear();
			AssetSelectWidget<BaseRoutineData> widget = new AssetSelectWidget<BaseRoutineData>("Routine", GameAssetType.ROUTINE, new Supplier<GameAsset<BaseRoutineData>>() {
				@Override
				public GameAsset<BaseRoutineData> get() {
					return component.getGameResource();
				}
			}, new PropertyWidget.ValueChanged<GameAsset<BaseRoutineData>>() {
				@Override
				public void report(GameAsset<BaseRoutineData> value) {
					component.setGameAsset(value);
					GameObject gameObject = component.getGameObject();
					SceneUtils.componentUpdated(gameObject.getGameObjectContainerRoot(), gameObject, component);
				}
			});

			properties.add(widget);

			PropertyWidget sizeWidget = WidgetFactory.generate(this, "viewportSize", "Viewport");
			properties.add(sizeWidget);

			PropertyWidget cacheWidget = WidgetFactory.generate(this, "cacheCoolDown", "Cache");
			properties.add(cacheWidget);

			Array<PropertyWidget> superList = super.getListOfProperties();
			properties.addAll(superList);

			for (PropertyWrapper<?> propertyWrapper : propertyWrappers) {
				PropertyWidget generate = WidgetFactory.generateForPropertyWrapper(propertyWrapper);
				generate.setInjectedChangeListener(new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
						propertyWrapper.isValueOverridden = true;
						component.routineInstance.setDirty();
					}
				});
				generate.setParent(this);
				properties.add(generate);
			}

			return properties;
		}

		@Override
		public String getPropertyBoxTitle() {
			return "Routine Renderer";
		}

		@Override
		public int getPriority() {
			return 4;
		}

	}
	
	private static class ScriptComponentProvider extends AComponentProvider<ScriptComponent> {

		public ScriptComponentProvider (ScriptComponent component) {
			super(component);
		}
		
		@Override
		public Array<PropertyWidget> getListOfProperties () {
			Array<PropertyWidget> properties = new Array<>();

			AssetSelectWidget<String> widget = new AssetSelectWidget<String>("Script", GameAssetType.SCRIPT, new Supplier<GameAsset<String>>() {
				@Override
				public GameAsset<String> get () {
					return component.getScriptResource();
				}
			}, new PropertyWidget.ValueChanged<GameAsset<String>>() {
				@Override
				public void report (GameAsset<String> value) {
					component.setGameAsset(value);
				}
			});

			properties.add(widget);

			for (PropertyWrapper<?> scriptProperty : component.getScriptProperties()) {
				PropertyWidget generate = WidgetFactory.generateForPropertyWrapper(scriptProperty);
				generate.setParent(this);
				properties.add(generate);
			}

			return properties;
		}

		@Override
		public String getPropertyBoxTitle () {
			return "Script Component";
		}

		@Override
		public int getPriority () {
			return 4;
		}
	}

	private static class SpineComponentProvider extends RendererComponentProvider<SpineRendererComponent> {

		public SpineComponentProvider (SpineRendererComponent component) {
			super(component);
		}

		@Override
		public Array<PropertyWidget> getListOfProperties () {
			Array<PropertyWidget> properties = new Array<>();

			AssetSelectWidget<SkeletonData> atlasWidget = new AssetSelectWidget<>("Skeleton", GameAssetType.SKELETON, new Supplier<GameAsset<SkeletonData>>() {
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

			properties.add(WidgetFactory.generate(this, "scale", "Scale"));

			PropertyWidget colorWidget = WidgetFactory.generate(this, "color", "Color");
			properties.add(colorWidget);

			PropertyWidget inheritParentColorWidget = WidgetFactory.generate(this, "shouldInheritParentColor", "Inherit Parent Color");
			properties.add(inheritParentColorWidget);

			SelectBoxWidget animSelectWidget = new SelectBoxWidget("Animation", new Supplier<String>() {
				@Override
				public String get() {
					if(component.animationState != null && component.animationState.getCurrent(0) != null && component.animationState.getCurrent(0).getAnimation() != null) {
						return component.animationState.getCurrent(0).getAnimation().getName();
					} else {
						return "";
					}
				}
			}, new PropertyWidget.ValueChanged<String>() {
				@Override
				public void report(String value) {
					Animation animation = component.skeleton.getData().findAnimation(value);
					component.animationState.setAnimation(0, animation, true);
					component.currAnimation = value;
				}
			}, new Supplier<Array<String>>() {
				@Override
				public Array<String> get() {
					Array<String> names = new Array<>();
					if(component.skeleton == null || component.skeleton.getData() == null) {
						return names;
					}
					Array<Animation> animations = component.skeleton.getData().getAnimations();
					for(Animation animation: animations) {
						names.add(animation.getName());
					}
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

	private static final class SpriteRendererComponentProvider extends RendererComponentProvider<SpriteRendererComponent> {


		public SpriteRendererComponentProvider (SpriteRendererComponent component) {
			super(component);
		}

		@Override
		public Array<PropertyWidget> getListOfProperties () {
			Array<PropertyWidget> properties = new Array<>();

			AssetSelectWidget<Texture> textureWidget = new AssetSelectWidget<>("Texture", GameAssetType.SPRITE, new Supplier<GameAsset<Texture>>() {
				@Override
				public GameAsset<Texture> get() {
					return component.getGameResource();
				}
			}, new PropertyWidget.ValueChanged<GameAsset<Texture>>() {
				@Override
				public void report(GameAsset<Texture> value) {
					component.setGameAsset(value);
				}
			});

			PropertyWidget colorWidget = WidgetFactory.generate(this, "color", "Color");
			PropertyWidget inheritParentColorWidget = WidgetFactory.generate(this, "shouldInheritParentColor", "Inherit Parent Color");
			PropertyWidget flipXWidget = WidgetFactory.generate(this, "flipX", "Flip X");
			PropertyWidget flipYWidget = WidgetFactory.generate(this, "flipY", "Flip Y");
			PropertyWidget fixAspectRatioWidget = WidgetFactory.generate(this, "fixAspectRatio", "Fix Aspect Ratio");
			PropertyWidget renderModesWidget = WidgetFactory.generate(this, "renderMode", "Render Mode");
			PropertyWidget sizeWidget = WidgetFactory.generate(this, "size", "Size");
			PropertyWidget tileSizeWidget = WidgetFactory.generate(this, "tileSize", "Tile Size");

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
				public void changed(ChangeEvent event, Actor actor) {
					if (!component.fixAspectRatio) return;

					final Texture texture = component.getGameResource().getResource();

					if (texture != null) {
						final float aspect = texture.getHeight() * 1f / texture.getWidth();
						component.size.y = component.size.x * aspect;
					}

					final ValueWidget yValue = ((Vector2PropertyWidget) sizeWidget).yValue;
					yValue.setValue(component.size.y, false);
				}
			});

			// change size by aspect ratio if aspect ratio is fixed
			sizeWidget.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					if (!component.fixAspectRatio) return;

					if (event.getTarget() instanceof ValueWidget) {
						final Vector2PropertyWidget vector2PropertyWidget = ((Vector2PropertyWidget) sizeWidget);
						final ValueWidget xValue = vector2PropertyWidget.xValue;
						final ValueWidget yValue = vector2PropertyWidget.yValue;
						final Texture texture = component.getGameResource().getResource();

						if (texture != null) {
							final float aspect = texture.getHeight() * 1f / texture.getWidth();

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

		@Override
		public String getPropertyBoxTitle () {
			return "Sprite Renderer";
		}

		@Override
		public int getPriority () {
			return 2;
		}

	}

	private static class TileDataComponentProvider extends AComponentProvider<TileDataComponent> {


		public TileDataComponentProvider (TileDataComponent component) {
			super(component);
		}
		@Override
		public Array<PropertyWidget> getListOfProperties () {
			Array<PropertyWidget> properties = new Array<>();

			properties.add(WidgetFactory.generate(this, "fakeZ", "FakeZ"));
			properties.add(WidgetFactory.generate(this, "visualOffset", "VisualOffset"));

			return properties;
		}

		public ObjectSet<GridPosition> getParentTiles() {
			return component.parentTiles;
		}

		public void setParentTiles(ObjectSet<GridPosition> parentTiles) {
			component.parentTiles = parentTiles;
		}

		@Override
		public String getPropertyBoxTitle () {
			return "TileData Component";
		}

		@Override
		public int getPriority () {
			return 4;
		}

	}

	private static class TransformComponentProvider extends AComponentProvider<TransformComponent> {

		public TransformComponentProvider (TransformComponent component) {
			super(component);
		}
		@Override
		public Array<PropertyWidget> getListOfProperties () {
			Array<PropertyWidget> properties = new Array<>();

			PropertyWidget positionWidget = WidgetFactory.generate(this, "position", "Position");
			PropertyWidget rotationWidget = WidgetFactory.generate(this, "rotation", "Rotation");
			PropertyWidget scaleWidget = WidgetFactory.generate(this, "scale", "Scale");

			properties.add(positionWidget);
			properties.add(rotationWidget);
			properties.add(scaleWidget);

			return properties;
		}

		@Override
		public String getPropertyBoxTitle () {
			return "Transform";
		}

		@Override
		public int getPriority () {
			return 1;
		}


	}


	private static class GameObjectPropertyProvider implements IPropertyProvider {

		private final GameObject gameObject;

		public GameObjectPropertyProvider (GameObject gameObject) {
			this.gameObject = gameObject;
		}

		@Override
		public Array<PropertyWidget> getListOfProperties () {
			Array<PropertyWidget> properties = new Array<>();

			LabelWidget uuidWidget = new LabelWidget("UUID", new Supplier<String>() {
				@Override
				public String get () {
					return gameObject.uuid.toString();
				}
			});

			EditableLabelWidget labelWidget = new EditableLabelWidget("Name", new Supplier<String>() {
				@Override
				public String get() {
					return gameObject.getName();
				}
			}, new PropertyWidget.ValueChanged<String>() {
				@Override
				public void report(String value) {
					GONameChangeCommand command = Notifications.obtainEvent(GONameChangeCommand.class).set(gameObject.getGameObjectContainerRoot(), gameObject, value);
					Notifications.fireEvent(command);
				}
			});

			properties.add(labelWidget);
			properties.add(uuidWidget);

			PropertyWidget activeWidget = WidgetFactory.generate(this, "active", "Active");
			activeWidget.addListener(new ChangeListener() {
				@Override
				public void changed (ChangeEvent event, Actor actor) {
					GameObjectActiveChanged activeChanged = Notifications.obtainEvent(GameObjectActiveChanged.class);
					activeChanged.target = gameObject;
					Notifications.fireEvent(activeChanged);
				}
			});
			properties.add(activeWidget);

			return properties;
		}

		@Override
		public String getPropertyBoxTitle () {
			return "Game Object";
		}

		@Override
		public int getPriority () {
			return 0;
		}

		@Override
		public Class<? extends IPropertyProvider> getType () {
			return getClass();
		}
	}

	private static class ScenePropertyProvider implements IPropertyProvider {


		private final Scene scene;

		public ScenePropertyProvider (Scene scene) {
			this.scene = scene;
		}

		@Override
		public Array<PropertyWidget> getListOfProperties () {
			Array<PropertyWidget> properties = new Array<>();

			LabelWidget labelWidget = new LabelWidget("Name", new Supplier<String>() {
				@Override
				public String get() {
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
			DynamicItemListWidget<ItemData> itemListWidget = new DynamicItemListWidget<ItemData>("Layers" , new Supplier<Array<ItemData>>() {
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
				public boolean canDelete(ItemData itemData) {
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


}


