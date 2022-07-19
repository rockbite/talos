package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;

public interface GameResourceOwner<U> {

	GameAssetType getGameAssetType();

	GameAsset<U> getGameResource ();
	void setGameAsset (GameAsset<U> gameAsset);

	static <U> void writeGameAsset (Json json, GameResourceOwner<U> owner) {
		json.writeValue("gameResource", owner.getGameResource().nameIdentifier);
	}

	static String readGameResourceFromComponent (JsonValue component) {
		return component.getString("gameResource", "broken");
	}

}
