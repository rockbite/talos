package com.talosvfx.talos.runtime.scene.components;

import com.talosvfx.talos.runtime.scene.GameObject;

public abstract class AComponent {
	private transient GameObject gameObject;

	public void setGameObject (GameObject gameObject) {
		this.gameObject = gameObject;
	}

	public GameObject getGameObject () {
		return gameObject;
	}

	public void reset () {
	}

	public void remove () {
		if (gameObject != null) {
			gameObject.removeComponent(this);
		}
	}

	public boolean allowsMultipleOfTypeOnGameObject () {
		return false;
	}

}
