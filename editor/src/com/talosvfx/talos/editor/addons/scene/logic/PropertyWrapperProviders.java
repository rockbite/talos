package com.talosvfx.talos.editor.addons.scene.logic;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Constructor;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.editor.addons.scene.apps.routines.providers.RoutinePropertyHolder;
import com.talosvfx.talos.editor.addons.scene.apps.routines.providers.RoutinePropertyProvider;
import com.talosvfx.talos.editor.addons.scene.logic.componentwrappers.*;
import com.talosvfx.talos.editor.addons.scene.logic.metawrappers.AMetaDataHolder;
import com.talosvfx.talos.editor.addons.scene.logic.metawrappers.AMetaDataProvider;
import com.talosvfx.talos.editor.addons.scene.logic.metawrappers.PrefabMetaDataHolder;
import com.talosvfx.talos.editor.addons.scene.logic.metawrappers.SpineMetaDataProvider;
import com.talosvfx.talos.editor.addons.scene.logic.metawrappers.SpriteMetaDataProvider;
import com.talosvfx.talos.editor.data.RoutineStageData;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.runtime.assets.meta.AtlasMetadata;
import com.talosvfx.talos.runtime.assets.meta.DirectoryMetadata;
import com.talosvfx.talos.runtime.assets.meta.EmptyMetadata;
import com.talosvfx.talos.runtime.assets.meta.PaletteMetadata;
import com.talosvfx.talos.runtime.assets.meta.PrefabMetadata;
import com.talosvfx.talos.runtime.assets.meta.SceneMetadata;
import com.talosvfx.talos.runtime.assets.meta.ScriptMetadata;
import com.talosvfx.talos.runtime.assets.meta.SpineMetadata;
import com.talosvfx.talos.runtime.assets.meta.SpriteMetadata;
import com.talosvfx.talos.runtime.assets.meta.TlsMetadata;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.Prefab;
import com.talosvfx.talos.runtime.scene.Scene;
import com.talosvfx.talos.runtime.scene.components.*;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyWrapperProviders {

	private static final Logger logger = LoggerFactory.getLogger(PropertyWrapperProviders.class);

	public static ObjectMap<Class<?>, Class<? extends IPropertyHolder>> propertyHoldersForClass = new ObjectMap<>();
	public static ObjectMap<Class<?>, Class<? extends IPropertyProvider>> propertyProvidersForClass = new ObjectMap<>();


	static {
		//objects
		propertyHoldersForClass.put(Scene.class, ScenePropertyHolder.class);
		propertyHoldersForClass.put(Prefab.class, PrefabPropertyHolder.class);
		propertyHoldersForClass.put(GameObject.class, GameObjectPropertyHolder.class);
		propertyHoldersForClass.put(RoutineStageData.class, RoutinePropertyHolder.class);

		//metas
		propertyHoldersForClass.put(AtlasMetadata.class, AMetaDataHolder.class);
		propertyHoldersForClass.put(DirectoryMetadata.class, AMetaDataHolder.class);
		propertyHoldersForClass.put(EmptyMetadata.class, AMetaDataHolder.class);
		propertyHoldersForClass.put(PaletteMetadata.class, AMetaDataHolder.class);
		propertyHoldersForClass.put(PrefabMetadata.class, PrefabMetaDataHolder.class);
		propertyHoldersForClass.put(SceneMetadata.class, AMetaDataHolder.class);
		propertyHoldersForClass.put(ScriptMetadata.class, AMetaDataHolder.class);
		propertyHoldersForClass.put(SpineMetadata.class, AMetaDataHolder.class);
		propertyHoldersForClass.put(SpriteMetadata.class, AMetaDataHolder.class);
		propertyHoldersForClass.put(TlsMetadata.class, AMetaDataHolder.class);


		//objects
		propertyProvidersForClass.put(Scene.class, ScenePropertyProvider.class);
		propertyProvidersForClass.put(GameObject.class, GameObjectPropertyProvider.class);
		propertyProvidersForClass.put(RoutineStageData.class, RoutinePropertyProvider.class);

		//metas=
		propertyProvidersForClass.put(AtlasMetadata.class, AMetaDataProvider.class);
		propertyProvidersForClass.put(DirectoryMetadata.class, AMetaDataProvider.class);
		propertyProvidersForClass.put(EmptyMetadata.class, AMetaDataProvider.class);
		propertyProvidersForClass.put(PaletteMetadata.class, AMetaDataProvider.class);
		propertyProvidersForClass.put(PrefabMetadata.class, AMetaDataProvider.class);
		propertyProvidersForClass.put(SceneMetadata.class, AMetaDataProvider.class);
		propertyProvidersForClass.put(ScriptMetadata.class, AMetaDataProvider.class);
		propertyProvidersForClass.put(SpineMetadata.class, SpineMetaDataProvider.class);
		propertyProvidersForClass.put(SpriteMetadata.class, SpriteMetaDataProvider.class);
		propertyProvidersForClass.put(TlsMetadata.class, AMetaDataProvider.class);

		//Components
		propertyProvidersForClass.put(CameraComponent.class, CameraComponentProvider.class);
		propertyProvidersForClass.put(CurveComponent.class, CurveComponentProvider.class);
		propertyProvidersForClass.put(MapComponent.class, MapComponentProvider.class);
		propertyProvidersForClass.put(PaintSurfaceComponent.class, PaintSurfaceComponentProvider.class);
		propertyProvidersForClass.put(ParticleComponent.class, ParticleComponentProvider.class);
		propertyProvidersForClass.put(RendererComponent.class, RendererComponentProvider.class);
		propertyProvidersForClass.put(RoutineRendererComponent.class, RoutineRenderComponentProvider.class);
		propertyProvidersForClass.put(ScriptComponent.class, ScriptComponentProvider.class);
		propertyProvidersForClass.put(SpineRendererComponent.class, SpineComponentProvider.class);
		propertyProvidersForClass.put(SpriteRendererComponent.class, SpriteRendererComponentProvider.class);
		propertyProvidersForClass.put(TileDataComponent.class, TileDataComponentProvider.class);
		propertyProvidersForClass.put(TransformComponent.class, TransformComponentProvider.class);
		propertyProvidersForClass.put(DataComponent.class, DataComponentProvider.class);
		propertyProvidersForClass.put(PathRendererComponent.class, PathRendererComponentProvider.class);


		//Routine Nodes

		boolean componentSafetyCheckFailed = false;
		try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(AComponent.class.getPackage().getName())
			.scan()) {
			ClassInfoList directBoxes = scanResult.getSubclasses(AComponent.class);
			for (ClassInfo directBox : directBoxes) {
				try {
					Class clazz = ClassReflection.forName(directBox.getName());
					if (!propertyProvidersForClass.containsKey(clazz)) {
						componentSafetyCheckFailed = true;
						logger.error("Must provide this class in property providers with its provider class companion, {}", clazz);
					}
				} catch (ReflectionException e) {
					throw new RuntimeException(e);
				}

			}
		}
		if (componentSafetyCheckFailed) {
			throw new GdxRuntimeException("Must provide this class in property providers with its provider class companion. see log for all errors");
		}
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
				Constructor[] constructors = ClassReflection.getConstructors(iPropertyProvider);
				for (Constructor constructor : constructors) {
					Class[] parameterTypes = constructor.getParameterTypes();
					if (parameterTypes.length == 1) {
						if (parameterTypes[0].isAssignableFrom(clazz)) {
							IPropertyHolder propertyHolder = (IPropertyHolder)constructor.newInstance(object);
							propertyHolderCache.put(object, propertyHolder);
							return propertyHolder;
						}
					} else {
						return (IPropertyHolder)constructor.newInstance();
					}
				}
			} catch (ReflectionException e) {
				throw new RuntimeException(e);
			}
		} else if (object instanceof IPropertyHolder) {
			IPropertyHolder propertyHolder = (IPropertyHolder) object;
			return propertyHolder;
		} else {
			logger.error("No such property provider for type: {}", clazz);
			throw new GdxRuntimeException("Holder must be registered in PropertyWrapperProviders");
		}

		logger.error("Holder must be registered in PropertyWrapperProviders for type: {}", clazz);
		throw new GdxRuntimeException("Holder must be registered in PropertyWrapperProviders");
	}

	public static IPropertyProvider getOrCreateProvider (Object object) {
		if (propertyProviderCache.containsKey(object)) {
			return propertyProviderCache.get(object);
		}
		Class<?> clazz = object.getClass();
		if (propertyProvidersForClass.containsKey(clazz)) {
			try {
				Class<? extends IPropertyProvider> iPropertyProvider = propertyProvidersForClass.get(clazz);
				Constructor[] constructors = ClassReflection.getConstructors(iPropertyProvider);
				for (Constructor constructor : constructors) {
					Class[] parameterTypes = constructor.getParameterTypes();
					if (parameterTypes.length == 1) {
						if (parameterTypes[0].isAssignableFrom(clazz)) {
							IPropertyProvider propertyProvider = (IPropertyProvider)constructor.newInstance(object);
							propertyProviderCache.put(object, propertyProvider);
							return propertyProvider;
						}
					} else {
						return (IPropertyProvider)constructor.newInstance();
					}
				}
			} catch (ReflectionException e) {
				throw new RuntimeException(e);
			}
		} else {
			logger.error("No such property provider for type: {}", clazz);
			throw new GdxRuntimeException("Provider must be registered in PropertyWrapperProviders");
		}

		throw new GdxRuntimeException("Provider must be registered in PropertyWrapperProviders");
	}


	public abstract static class ObjectPropertyHolder<T> implements IPropertyHolder {

	}

}


