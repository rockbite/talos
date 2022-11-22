package com.talosvfx.talos.editor.addons.scene.assets;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.editor.addons.scene.logic.SceneMetadata;
import com.talosvfx.talos.editor.addons.scene.logic.components.GameResourceOwner;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.*;

public enum GameAssetType {
	SPRITE(new String[]{"png", "jpg", "jpeg"}, true),
	ATLAS(new String[]{"atlas"}, true),
	SKELETON(new String[]{"skeleton", "skel", "skele"}, true),
	SOUND(new String[]{"mp3", "ogg", "m4a", "wav"}, true),
	VFX(new String[]{"tls"}, true),
	VFX_OUTPUT(new String[]{"p"}, false),
	SCRIPT(new String[]{"ts", "js"}, true),
	TWEEN(new String[]{"tw"}, true),
	PREFAB(new String[]{"prefab"}, true),
	SCENE(new String[]{"scn"}, true),
	DIRECTORY(new String[]{}, false),
	TILE_PALETTE(new String[]{"ttp"}, true);

	private ObjectSet<String> extensions;
	private boolean isRootGameAsset;

	GameAssetType (String[] extensionArray, boolean isRootGameAsset) {
		extensions = new ObjectSet<String>();
		extensions.addAll(extensionArray);
		this.isRootGameAsset = isRootGameAsset;
	}

	public static class NoAssetTypeException extends Exception {
		public NoAssetTypeException (String message) {
			super(message);
		}
	}

	public static GameAssetType getAssetTypeFromGameResourceOwner (Class<? extends GameResourceOwner> aClass) {
		try {
			return ClassReflection.newInstance(aClass).getGameAssetType();
		} catch (ReflectionException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isRootGameAsset () {
		return this.isRootGameAsset;
	}

	public static GameAssetType getAssetTypeFromExtension (String extension) throws NoAssetTypeException {
		for (GameAssetType value : GameAssetType.values()) {
			if (value.extensions.contains(extension)) {
				return value;
			}
		}

		throw new NoAssetTypeException("No asset found for extension: " + extension);
	}

	public static Class<? extends AMetadata> getMetaClassForType (GameAssetType type) {
		switch (type) {
		case SPRITE:
			return SpriteMetadata.class;
		case ATLAS:
			return AtlasMetadata.class;
		case SKELETON:
			return SpineMetadata.class;
		case VFX:
			return TlsMetadata.class;
		case PREFAB:
			return PrefabMetadata.class;
		case SCENE:
			return SceneMetadata.class;
		case DIRECTORY:
			return DirectoryMetadata.class;
		case SCRIPT:
			return ScriptMetadata.class;
		case TILE_PALETTE:
			return PaletteMetadata.class;
		case VFX_OUTPUT:
		case TWEEN:
		case SOUND:
			return EmptyMetadata.class;
		}
		throw new GdxRuntimeException("No meta data method found for extension: " + type);
	}

	public static AMetadata createMetaForType (GameAssetType type) {
		try {
			return ClassReflection.newInstance(getMetaClassForType(type));
		} catch (ReflectionException e) {
			throw new RuntimeException(e);
		}
	}
}
