package com.talosvfx.talos.editor.project2.apps.preferences;

import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.runtime.assets.GameAsset;

/**
 * App preferences per asset
 */
public class AppPrefs {
    private ObjectMap<String, AppPreference> preferences;

    public AppPrefs () {
        preferences = new ObjectMap<>();
    }

    public boolean hasPrefFor(GameAsset<?> gameAsset) {
        if (gameAsset.nameIdentifier.equals("dummy")) return false;
        return preferences.containsKey(getUUID(gameAsset));
    }

    public AppPreference getPrefFor(GameAsset<?> gameAsset) {
        return preferences.get(getUUID(gameAsset));
    }

    public void setPrefFor(GameAsset<?> gameAsset, AppPreference appPref) {
        preferences.put(getUUID(gameAsset), appPref);
    }

    private String getUUID (GameAsset<?> gameAsset) {
        return gameAsset.getRootRawAsset().metaData.uuid.toString();
    }

    /**
     * Tag interface.
     */
    public interface AppPreference {}
}

