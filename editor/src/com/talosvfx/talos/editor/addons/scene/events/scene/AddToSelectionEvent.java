package com.talosvfx.talos.editor.addons.scene.events.scene;

import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.GameObjectContainer;
import com.talosvfx.talos.editor.notifications.ContextRequiredEvent;
import com.talosvfx.talos.editor.notifications.TalosEvent;
import lombok.Data;

public class AddToSelectionEvent extends ContextRequiredEvent<GameObjectContainer> {

	private GameObject gameObject;

	public AddToSelectionEvent set (GameObjectContainer context, GameObject gameObject) {
		setContext(context);
		this.gameObject = gameObject;

		return this;
	}

	public GameObject getGameObject () {
		return gameObject;
	}

	@Override
	public void reset () {
		gameObject = null;
	}
}
