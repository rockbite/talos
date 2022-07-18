package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;

public interface GameResourceOwner<U> {

	GameAsset<U> getGameResource ();
	void setGameAsset (GameAsset<U> gameAsset);

	static <U> void writeGameAsset (Json json, GameResourceOwner<U> owner) {
		json.writeValue("gameResource", owner.getGameResource().nameIdentifier);
	}

	static String readGameResourceFromComponent (JsonValue component) {
		return component.getString("gameResource", "broken");
	}

}