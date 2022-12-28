package com.talosvfx.talos.editor.project2.apps;

import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;

/**
 * App preferences per asset
 */
public class AppPrefs {
    private ObjectMap<String, Object> preferences;

    public AppPrefs () {
        preferences = new ObjectMap<>();
    }

    public boolean hasPrefFor(GameAsset<?> gameAsset) {
        return preferences.containsKey(getUUID(gameAsset));
    }

    public Object getPrefFor(GameAsset<?> gameAsset) {
        return preferences.get(getUUID(gameAsset));
    }

    public void setPrefFor(GameAsset<?> gameAsset, Object appPref) {
        preferences.put(getUUID(gameAsset), appPref);
    }

    private String getUUID (GameAsset<?> gameAsset) {
        if (!gameAsset.dependentRawAssets.isEmpty()) {
            return gameAsset.getRootRawAsset().metaData.uuid.toString();
        } else {
            return "singleton";
        }
    }
}

