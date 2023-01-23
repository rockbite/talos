package com.talosvfx.talos.editor.addons.scene.events.scene;

import com.talosvfx.talos.runtime.scene.GameObject;import com.talosvfx.talos.editor.notifications.TalosEvent;
import lombok.Data;

@Data
public class SelectGameObjectExternallyEvent implements TalosEvent {

	private GameObject gameObject;

	@Override
	public void reset () {
		gameObject = null;
	}

	public SelectGameObjectExternallyEvent setGameObject (GameObject gameObject) {
		this.gameObject = gameObject;
		return this;
	}
}
